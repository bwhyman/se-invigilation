package com.se.invigilation.service;

import com.se.invigilation.dox.InviDetail;
import com.se.invigilation.dox.Invigilation;
import com.se.invigilation.dox.Timetable;
import com.se.invigilation.dox.User;
import com.se.invigilation.dto.AssignUserDTO;
import com.se.invigilation.dto.InviCountDTO;
import com.se.invigilation.repository.InviDetailRepository;
import com.se.invigilation.repository.InvigilationRepository;
import com.se.invigilation.repository.TimetableRepository;
import com.se.invigilation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubjectService {
    private final UserRepository userRepository;
    private final InvigilationRepository invigilationRepository;
    private final TimetableRepository timetableRepository;
    private final InviDetailRepository inviDetailRepository;

    //
    @Cacheable(value = "users", key = "{#depid}")
    public Mono<List<User>> listUsers(String depid) {
        return userRepository.findByDepId(depid).collectList().cache();
    }

    //
    public Mono<List<Invigilation>> listInvigilations(String depid, int status, Pageable pageable) {
        return invigilationRepository.findByDepIdAndStatus(depid, status, pageable).collectList();
    }

    public Mono<Integer> getInvisTotal(String depid, int status) {
        return invigilationRepository.findTotalByByDepIdAndStatus(depid, status);
    }

    //
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public Mono<List<Integer>> updateUserInviStatus(List<User> users) {
        List<Mono<Integer>> monos = new ArrayList<>();
        for (User user : users) {
            Mono<Integer> integerMono = invigilationRepository.updateInviStatus(user.getId(), user.getInviStatus());
            monos.add(integerMono);
        }
        return Flux.merge(monos).collectList();
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

    public Mono<List<Invigilation>> listInvigilations(String depid, LocalDate date) {
        return invigilationRepository.findByDepIdAndDate(depid, date).collectList();
    }

    @Transactional
    public Mono<Invigilation> addInvidetails(String inviid, AssignUserDTO assignUser) {
        // 删除原监考分配
        Mono<Integer> delInviDetailM = inviDetailRepository.deleteByInviId(inviid);
        // 创建新详细分配
        List<Mono<InviDetail>> monos = new ArrayList<>();
        for (User user : assignUser.getUsers()) {
            InviDetail d = InviDetail.builder()
                    .inviId(inviid)
                    .userId(user.getId())
                    .teacherName(user.getName())
                    .build();
            Mono<InviDetail> save = inviDetailRepository.save(d);
            monos.add(save);
        }
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

        return delInviDetailM.flatMap(r -> Flux.merge(monos).collectList())
                .flatMap(r -> updateInviM);
    }

    public Mono<List<User>> listInviDetailUsers(String inviid) {
        return inviDetailRepository.findByInviId(inviid).collectList();
    }

    @Transactional
    public Mono<String> updateInviCalanderId(String inviid, String calid, String createUnionId, String noticeIds) {
        return invigilationRepository.updateCalanderId(inviid, calid, createUnionId, noticeIds)
                .thenReturn(calid);
    }

    @Transactional
    public Mono<String> updateInviCalanderNull(String inviid) {
        return invigilationRepository.updateCalanderNull(inviid).thenReturn(inviid);
    }

}