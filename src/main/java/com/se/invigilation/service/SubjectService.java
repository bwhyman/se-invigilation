package com.se.invigilation.service;

import com.se.invigilation.dox.*;
import com.se.invigilation.dto.AssignUserDTO;
import com.se.invigilation.dto.InviCountDTO;
import com.se.invigilation.repository.*;
import io.r2dbc.spi.Statement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubjectService {
    private final UserRepository userRepository;
    private final InvigilationRepository invigilationRepository;
    private final DepartmentRepository departmentRepository;
    private final TimetableRepository timetableRepository;
    private final InviDetailRepository inviDetailRepository;
    private final ExcludeRuleRepository excludeRuleRepository;
    private final DatabaseClient databaseClient;
    private final SettingRepository settingRepository;

    //
    public Mono<List<User>> listUsers(String depid) {
        return userRepository.findByDepId(depid).collectList();
    }

    //
    public Mono<List<Invigilation>> listDispatchedInvigilations(String depid, int status, Pageable pageable) {
        return invigilationRepository.findByDepIdAndStatus(depid, status, pageable).collectList();
    }

    public Mono<List<Invigilation>> listDispatchedInvigilations(String depid) {
        return invigilationRepository.findDispatcheds(depid).collectList();
    }

    public Mono<Integer> getInvisTotal(String depid, int status) {
        return invigilationRepository.findTotalByByDepIdAndStatus(depid, status);
    }

    @Transactional
    public Mono<Void> updateUserInviStatus(List<User> users, String depid) {
        var sql = "update user u set u.invi_status=? where u.id=? and u.department ->> '$.depId'=?";
        return databaseClient
                .inConnection(conn -> {
                    Statement statement = conn.createStatement(sql);
                    for (int i = 0; i < users.size(); i++) {
                        statement.bind(0, users.get(i).getInviStatus())
                                .bind(1, users.get(i).getId())
                                .bind(2, depid);
                        // 最后一次不能调用add()方法
                        if (i < users.size() - 1) {
                            statement.add();
                        }
                    }
                    // 必须使用Flux聚合
                    return Flux.from(statement.execute()).collectList();
                }).then();
    }

    public Mono<Invigilation> getInvigilation(String depId, String inviid) {
        return invigilationRepository.findByDepId(depId, inviid);
    }

    //
    public Mono<List<Timetable>> listTimetable(String depid, int week, int dayweek) {
        return timetableRepository.findByDepIdAndDate(depid, week, dayweek, User.INVI_STATUS_OPEN)
                .collectList();
    }

    //
    public Mono<List<InviCountDTO>> listDepUserCounts(String depid) {
        return inviDetailRepository.findDepUserCounts(depid).collectList();
    }

    public Mono<List<Invigilation>> listDispatchedInvigilations(String depid, LocalDate date) {
        return invigilationRepository.findByDepIdAndDate(depid, date).collectList();
    }

    @Transactional
    public Mono<Void> addInvidetails(String inviid, AssignUserDTO assignUser) {
        // 删除原监考分配
        Mono<Integer> delInviDetailM = inviDetailRepository.deleteByInviId(inviid);
        // 创建新详细分配
        Mono<Void> DetailM = Flux.fromIterable(Arrays.asList(assignUser.getUserIds()))
                .flatMap(uid -> {
                    InviDetail d = InviDetail.builder()
                            .inviId(inviid)
                            .userId(uid)
                            .build();
                    return inviDetailRepository.save(d);
                }).then();

        // 更新监考信息
        Mono<Invigilation> updateInviM = invigilationRepository.findById(inviid)
                .flatMap(invi -> {
                    if (StringUtils.hasLength(assignUser.getDepartment())) {
                        invi.setDepartment(assignUser.getDepartment());
                    }
                    invi.setStatus(Invigilation.ASSIGN);
                    invi.setAllocator(assignUser.getAllocator());
                    invi.setExecutor(assignUser.getExecutor());
                    return invigilationRepository.save(invi);
                });

        return delInviDetailM
                .then(DetailM).
                then(updateInviM).then();
    }

    public Mono<List<User>> listInviDetailUsers(String inviid) {
        return userRepository.findByInviId(inviid).collectList();
    }

    @Transactional
    public Mono<String> updateInviCalanderId(String inviid, String calid, String createUnionId, String noticeIds) {
        return invigilationRepository.updateCalanderId(inviid, calid, createUnionId, noticeIds)
                .thenReturn(calid);
    }

    @Transactional
    public Mono<String> updateInviCalanderNull(String inviid, String depid) {
        return invigilationRepository.updateCalanderNull(inviid, depid).thenReturn(inviid);
    }

    @Transactional
    public Mono<Void> updateComment(String depid, String comment) {
        return departmentRepository.updateComment(depid, comment).then();
    }

    public Mono<String> getDepartmentComment(String depid) {
        return departmentRepository.findCommentByDepid(depid);
    }

    public Mono<List<ExcludeRule>> listExcludeRules(String depid) {
        return excludeRuleRepository.findByDepIdOrderByUserId(depid).collectList();
    }

    public Mono<Void> addExculdeRule(ExcludeRule rule) {
        return excludeRuleRepository.save(rule).then();
    }

    @Transactional
    public Mono<Void> removeExculdeRule(String rid) {
        return excludeRuleRepository.deleteById(rid).then();
    }

    public Mono<List<Invigilation>> listInvisByDateByDepId(String depid, String sdate, String edate) {
        return invigilationRepository.findByDateByDepId(depid, sdate, edate)
                .collectList();
    }
    public Mono<List<Invigilation>> listInvisByDepId(String depid) {
        return invigilationRepository.findByDepId(depid).collectList();
    }

    public Mono<List<Setting>> listSettings(int state) {
        return settingRepository.findByState(state).collectList();
    }
}