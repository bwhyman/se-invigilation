package com.se.invigilation.service;

import com.se.invigilation.component.SnowflakeGenerator;
import com.se.invigilation.dox.*;
import com.se.invigilation.dto.AssignUserDTO;
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

import java.util.Arrays;
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
    public Mono<List<Invigilation>> addInvigilations(List<Invigilation> invigilations) {
        return invigilationRepository.saveAll(invigilations).collectList();
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
    public Mono<Void> updateDispatcher(List<Invigilation> invigilations, String collid) {
        return Flux.fromIterable(invigilations)
                .flatMap(invi -> {
                    invi.setStatus(Invigilation.DISPATCH);
                    return invigilationRepository.updateInvi(invi, collid);
                }).then();
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
                .then(databaseClient.inConnection(conn -> {
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
                }).then());
    }

    public Mono<List<User>> listUsers(String depid, String collid, String role) {
        return userRepository.findByDepidAndrole(depid, collid, role).collectList();
    }

    @Transactional
    public Mono<Void> addTimetable(String userid, List<Timetable> timetables) {
        return timetableRepository.deleteByUserId(userid)
                .then(timetableRepository.saveAll(timetables).then());
    }

    @Transactional
    public Mono<Void> removeInvigilation(String inviid, String collid) {
        return invigilationRepository.deleteInvi(inviid, collid).then();
    }

    @Transactional
    public Mono<Invigilation> updateInvigilations(Invigilation invigilation) {
        return invigilationRepository.findById(invigilation.getId())
                .flatMap((invi) -> {
                    invi.setAmount(invigilation.getAmount());
                    invi.setDate(invigilation.getDate());
                    invi.setTime(invigilation.getTime());
                    invi.setCourse(invigilation.getCourse());
                    return invigilationRepository.save(invi);
                });
    }

    @Transactional
    public Mono<Void> resetInvigilation(String inviid) {
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
                })
                .then(inviDetailRepository.deleteByInviId(inviid))
                .then();
    }

    @Transactional
    public Mono<Void> updateDepartmentInviStatus(List<Department> departments) {
        return Flux.fromIterable(departments)
                .flatMap(depart -> departmentRepository
                        .updateInviStatusById(depart.getId(), depart.getInviStatus())).then();
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
    public Mono<Void> updatePassword(String account, String collid) {
        return userRepository.updatePassword(account, collid, passwordEncoder.encode(account)).then();
    }

    @Transactional
    public Mono<Void> addUser(User user) {
        return userRepository.save(user).then();
    }

    public Mono<List<Invigilation>> listInvisByDateByCollId(String collid, String sdate, String edate) {
        return invigilationRepository.findByDateByCollId(collid, sdate, edate)
                .collectList();
    }

    @Transactional
    public Mono<Void> updateInviRemark(List<String> inviIds, String remark) {
        return invigilationRepository.updateRemarks(inviIds, remark).then();
    }

    public Mono<Invigilation> getInvigilation(String collid, String inviid) {
        return invigilationRepository.findByCollId(collid, inviid);
    }

    @Transactional
    public Mono<Void> updateInvigilations(String oldInviid, Invigilation invi) {
        return invigilationRepository.updateAmount(oldInviid)
                .then(invigilationRepository.save(invi))
                .then();
    }

    public Mono<Void> removeUser(String uid, String collid) {
        return userRepository.deleteById(uid, collid).then();
    }

    @Transactional
    public Mono<Void> removeCollegeData(String collid) {
        Mono<Integer> inviM = invigilationRepository.deleteInvis(collid);
        Mono<Integer> timetableM = timetableRepository.deleteByCollId(collid);
        return Mono.when(inviM, timetableM);
    }

    @Transactional
    public Mono<Void> removeDepartment(String did, String collid) {
        return departmentRepository.deleteById(did).then();
    }

    @Transactional
    public Mono<Void> updateDetparmentName(String depId, String collId, String name) {
        Mono<Integer> dM = departmentRepository.updateName(depId, collId, name);
        Mono<Integer> uM = userRepository.updateUsersDepartment(depId, collId, name);
        Mono<Integer> inviM = invigilationRepository.updateDepartmentName(depId, collId, name);
        return Mono.when(dM, uM, inviM).then();
    }

    @Transactional
    public Mono<Invigilation> assignInvilaton(String collid, String inviid, AssignUserDTO assignUserDTO) {
        Mono<Integer> delInviDetailM = inviDetailRepository.deleteByInviId(inviid);
        // 创建新详细分配
        Mono<Void> detailM = Flux.fromIterable(Arrays.asList(assignUserDTO.getUserIds()))
                .flatMap(uid -> {
                    InviDetail d = InviDetail.builder()
                            .inviId(inviid)
                            .userId(uid)
                            .build();
                    return inviDetailRepository.save(d);
                }).then();
        Mono<Invigilation> invigilationMono = invigilationRepository.findByCollId(collid, inviid)
                .flatMap(invi -> {
                    invi.setDepartment(assignUserDTO.getDepartment());
                    if (assignUserDTO.getAmount() != null) {
                        invi.setAmount(assignUserDTO.getAmount());
                    }
                    invi.setDispatcher(assignUserDTO.getDispatcher());
                    invi.setStatus(Invigilation.ASSIGN);
                    invi.setExecutor(assignUserDTO.getExecutor());
                    invi.setAllocator(assignUserDTO.getAllocator());
                    invi.setCalendarId(null);
                    invi.setCreateUnionId(null);
                    invi.setNoticeUserIds(null);
                    return invigilationRepository.save(invi);
                });

        return delInviDetailM.then(detailM).then(invigilationMono);
    }

    @Transactional
    public Mono<User> updateUser(String uid, User user, String collid) {
        return userRepository.findByCollId(uid, collid)
                .flatMap(u -> {
                    if (user.getDepartment() != null) {
                        u.setDepartment(user.getDepartment());
                    }
                    if (user.getRole() != null) {
                        u.setRole(user.getRole());
                    }
                    if (user.getName() != null) {
                        u.setName(user.getName());
                    }
                    if (user.getDingUserId() != null) {
                        u.setDingUserId(user.getDingUserId());
                    }
                    if (user.getDingUnionId() != null) {
                        u.setDingUnionId(user.getDingUnionId());
                    }
                    if (user.getMobile() != null) {
                        u.setMobile(user.getMobile());
                    }
                    return userRepository.save(u);
                });
    }
}
