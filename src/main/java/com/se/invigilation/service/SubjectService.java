package com.se.invigilation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final ObjectMapper objectMapper;

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
    @SneakyThrows
    public Mono<Invigilation> addInvidetails(String inviid, String depid, AssignUserDTO assignUser) {
        String exec = objectMapper.writeValueAsString(assignUser.getExecutor());
        // 删除原监考分配
        Mono<Integer> delInviDetailM = inviDetailRepository.deleteByInviId(inviid);
        // 创建新详细分配
        List<Mono<InviDetail>> monos = new ArrayList<>();
        for (AssignUserDTO.AssignUser user : assignUser.getExecutor()) {
            InviDetail d = InviDetail.builder()
                    .inviId(inviid)
                    .depId(depid)
                    .userId(user.getUserId())
                    .teacherName(user.getUserName())
                    .build();
            Mono<InviDetail> save = inviDetailRepository.save(d);
            monos.add(save);
        }
        // 更新监考信息
        Mono<Invigilation> updateInviM = invigilationRepository.findById(inviid)
                .flatMap(invi -> {
                    invi.setStatus(Invigilation.ASSIGN);
                    invi.setAllocator(assignUser.getAllocator());
                    invi.setExecutor(exec);
                    return invigilationRepository.save(invi);
                });

        return delInviDetailM.flatMap(r -> Flux.merge(monos).collectList())
                .flatMap(r -> updateInviM);
    }

    public Mono<List<User>> listInviDetailUsers(String inviid) {
        return inviDetailRepository.findByInviId(inviid).collectList();
    }

    @Transactional
    public Mono<String> updateInviCalanderId(String inviid, String calid, String createUnionId) {
        return invigilationRepository.updateCalanderId(inviid, calid, createUnionId)
                .thenReturn(calid);
    }

    @Transactional
    public Mono<String> updateInviCalanderNull(String inviid) {
        return invigilationRepository.updateCalanderNull(inviid).thenReturn(inviid);
    }

}