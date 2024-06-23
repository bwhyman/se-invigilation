package com.se.invigilation.service;

import com.se.invigilation.component.SnowflakeGenerator;
import com.se.invigilation.dox.Department;
import com.se.invigilation.dox.Invigilation;
import com.se.invigilation.dox.Timetable;
import com.se.invigilation.dox.User;
import com.se.invigilation.dto.DepartmentDTO;
import com.se.invigilation.dto.InviCountDTO;
import com.se.invigilation.repository.*;
import io.r2dbc.spi.Statement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollegeService {
    private final InvigilationRepository invigilationRepository;
    private final InviDetailRepository inviDetailRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final TimetableRepository timetableRepository;
    private final PasswordEncoder passwordEncoder;
    private final DatabaseClient databaseClient;
    private final SnowflakeGenerator.Snowflake snowflake;

    @Transactional
    public Mono<Void> addInvigilations(List<Invigilation> invigilations) {
        return invigilationRepository.saveAll(invigilations).collectList().then();
    }

    @Transactional
    public Mono<Void> addInvigilation(Invigilation invigilation) {
        return invigilationRepository.save(invigilation).then();
    }

    public Mono<List<Department>> listDepartments(String collId, int status) {
        return departmentRepository.findByCollId(collId, status).collectList();
    }

    public Mono<List<Department>> listDepartments(String collId) {
        return departmentRepository.findByCollId(collId).collectList();
    }

    public Mono<List<Invigilation>> listImporteds(String collid) {
        return invigilationRepository.findimporteds(collid).collectList();
    }

    public Mono<Integer> getdispatchedTotal(String depid, String collid) {
        return invigilationRepository.findDispatchedTotal(depid, collid);
    }

    public Mono<List<Invigilation>> listDispatchedInvis(String depid, String collid, Pageable pageable) {
        return invigilationRepository.findDispatcheds(depid, collid, pageable).collectList();
    }

    @Transactional
    public Mono<List<Integer>> updateDispatcher(List<Invigilation> invigilations, String collid) {
        List<Mono<Integer>> monos = new ArrayList<>();
        for (Invigilation invi : invigilations) {
            invi.setStatus(Invigilation.DISPATCH);
            Mono<Integer> integerMono = invigilationRepository.updateInvi(invi, collid);
            monos.add(integerMono);
        }
        return Flux.merge(monos).collectList();
    }

    public Mono<List<User>> listUser(String collid) {
        return userRepository.findByCollId(collid).collectList();
    }

    @Transactional
    public Mono<Void> addTimetables(List<Timetable> timetables, String collid) {
        var sql = """
                insert into timetable
                (id, coll_id, startweek, endweek, dayweek, period, course, user_id, teacher_name)
                values(?,?,?,?,?,?,?,?,?)
                """;
        return timetableRepository.deleteByCollId(collid)
                .flatMap(r -> databaseClient.inConnection(conn -> {
                    Statement statement = conn.createStatement(sql);
                    for (int i = 0; i < timetables.size(); i++) {
                        var tb = timetables.get(i);
                        statement.bind(0, snowflake.nextId())
                                .bind(1, collid)
                                .bind(2, tb.getStartweek())
                                .bind(3, tb.getEndweek())
                                .bind(4, tb.getDayweek())
                                .bind(5, tb.getPeriod())
                                .bind(6, tb.getCourse())
                                .bind(7, tb.getUserId())
                                .bind(8, tb.getTeacherName());
                        // 最后一次不能调用add()方法
                        if (i < timetables.size() - 1) {
                            statement.add();
                        }
                    }
                    return Flux.from(statement.execute()).collectList();
                })).then();
    }

    public Mono<List<User>> listUsers(String depid, String collid, String role) {
        return userRepository.findByDepidAndrole(depid, collid, role).collectList();
    }

    @Transactional
    public Mono<Integer> addTimetable(String userid, List<Timetable> timetables) {
        return timetableRepository.deleteByUserId(userid).flatMap((r) ->
                timetableRepository.saveAll(timetables).collectList()
                        .thenReturn(1));
    }

    @Transactional
    public Mono<Integer> removeInvigilation(String inviid, String collid) {
        return invigilationRepository.deleteInvi(inviid, collid);
    }

    @Transactional
    public Mono<Integer> updateInvigilations(Invigilation invigilation) {
        return invigilationRepository.findById(invigilation.getId())
                .flatMap((invi) -> {
                    invi.setAmount(invigilation.getAmount());
                    invi.setDate(invigilation.getDate());
                    invi.setTime(invigilation.getTime());
                    invi.setCourse(invigilation.getCourse());
                    invi.setStatus(invigilation.getStatus());
                    return invigilationRepository.save(invi)
                            .thenReturn(1);
                });
    }

    @Transactional
    public Mono<Integer> resetInvigilation(String inviid) {
        return invigilationRepository.findById(inviid).flatMap((invi) -> {
            invi.setStatus(Invigilation.IMPORT);
            invi.setRemark(null);
            invi.setDepartment(null);
            invi.setDispatcher(null);
            invi.setAllocator(null);
            invi.setExecutor(null);
            invi.setCalendarId(null);
            invi.setCreateUnionId(null);
            invi.setNoticeUserIds(null);
            return invigilationRepository.save(invi);
        }).flatMap((r) -> inviDetailRepository.deleteByInviId(inviid));
    }

    @Transactional
    public Mono<Integer> updateDepartmentInviStatus(List<Department> departments) {
        List<Mono<Integer>> monos = new ArrayList<>();
        for (Department depart : departments) {
            Mono<Integer> integerMono = departmentRepository
                    .updateInviStatusById(depart.getId(), depart.getInviStatus());
            monos.add(integerMono);
        }
        return Flux.merge(monos).then(Mono.just(1));
    }

    public Mono<List<User>> listUsersByName(String depid, String name) {
        return userRepository.findByDepIdAndName(depid, name).collectList();
    }

    @Transactional
    public Mono<Integer> updateRole(String uid, String role, String collid) {
        return userRepository.updateRole(uid, role, collid);
    }

    public Mono<List<Invigilation>> listInvis(String collid) {
        return invigilationRepository.findByCollId(collid)
                .collectList();
    }

    public Mono<List<InviCountDTO>> listCollCounts(String collid) {
        return inviDetailRepository.findCollUserCounts(collid)
                .collectList();
    }

    @Transactional
    public Mono<Integer> updateUserDepartment(String uid, String collid, String depart) {
        return userRepository.updateDepartment(uid, collid, depart);
    }

    @Transactional
    public Mono<Integer> updatePassword(String account, String collid) {
        return userRepository.updatePassword(account, collid, passwordEncoder.encode(account));
    }

    @Transactional
    public Mono<Integer> addUser(User user) {
        return userRepository.save(user).thenReturn(1);
    }

    public Mono<List<Invigilation>> listInvisByDate(String collid, String sdate, String edate) {
        return invigilationRepository.findByDate(collid, sdate, edate)
                .collectList();
    }

    @Transactional
    public Mono<Integer> updateInviRemark(List<String> inviIds, String remark) {
        return invigilationRepository.updateRemarks(inviIds, remark);
    }

    public Mono<Invigilation> getInvigilation(String collid, String inviid) {
        return invigilationRepository.findByCollId(collid, inviid);
    }

    @Transactional
    public Mono<Integer> updateInvigilations(String oldInviid, Invigilation invi) {
        return invigilationRepository.updateAmount(oldInviid)
                .flatMap(r -> invigilationRepository.save(invi))
                .thenReturn(1);
    }

    public Mono<List<User>> getUser(String collid, String name) {
        return userRepository.findByName(collid, name).collectList();
    }

    public Mono<Integer> removeUser(String uid, String collid) {
        return userRepository.deleteById(uid, collid).thenReturn(1);
    }

    @Transactional
    public Mono<Void> removeCollegeData(String collid) {
        Mono<Integer> inviM = invigilationRepository.deleteInvis(collid);
        Mono<Integer> timetableM = timetableRepository.deleteByCollId(collid);
        return Mono.when(inviM, timetableM);
    }

    @Transactional
    public Mono<Integer> removeDepartment(String did, String collid) {
        return departmentRepository.deleteById(did).thenReturn(1);
    }

    @Transactional
    public Mono<Integer> updateDetparmentName(String depId, String collId, String name) {
        Mono<Integer> dM = departmentRepository.updateName(depId, collId, name);
        Mono<Integer> uM = userRepository.updateUsersDepartment(depId, collId, name);
        Mono<Integer> inviM = invigilationRepository.updateDepartmentName(depId, collId, name);
        return Mono.when(dM, uM, inviM).thenReturn(1);
    }
}
