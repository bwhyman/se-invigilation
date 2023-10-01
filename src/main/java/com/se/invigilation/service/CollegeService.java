package com.se.invigilation.service;

import com.se.invigilation.dox.Department;
import com.se.invigilation.dox.Invigilation;
import com.se.invigilation.dox.Timetable;
import com.se.invigilation.dox.User;
import com.se.invigilation.repository.DepartmentRepository;
import com.se.invigilation.repository.InvigilationRepository;
import com.se.invigilation.repository.TimetableRepository;
import com.se.invigilation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
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
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final TimetableRepository timetableRepository;

    @Transactional
    public Mono<Void> addInvigilations(List<Invigilation> invigilations) {
        return invigilationRepository.saveAll(invigilations).collectList().then();
    }
    @Transactional
    public Mono<Void> addInvigilation(Invigilation invigilation) {
        return invigilationRepository.save(invigilation).then();
    }

    public Mono<List<Department>> listDepartments(String collId) {
        return departmentRepository.findByCollId(collId).collectList();
    }

    public Mono<List<Invigilation>> listCollegeInvigilations(String collId, int status) {
        return invigilationRepository.findByCollIdAndStatus(collId, status).collectList();
    }

    @Transactional
    public Mono<List<Integer>> updateDispatcher(List<Invigilation> invigilations) {
        List<Mono<Integer>> monos = new ArrayList<>();
        for (Invigilation i : invigilations) {
            Mono<Integer> update = invigilationRepository.findById(i.getId())
                    .flatMap(invi -> {
                        invi.setDepartment(i.getDepartment());
                        invi.setStatus(i.getStatus());
                        invi.setDispatcher(i.getDispatcher());
                        return invigilationRepository.save(invi).thenReturn(1);
                    });
            monos.add(update);
        }
        return Flux.merge(monos).collectList();
    }

    public Mono<List<User>> listUser(String collid) {
        return userRepository.findByCollId(collid).collectList();
    }

    @Transactional
    public Mono<Void> addTimetables(List<Timetable> timetables) {
        return timetableRepository.saveAll(timetables).then();
    }

    public Mono<List<User>> listUsers(String depid, String role) {
        return userRepository.findByDepidAndrole(depid, role).collectList();
    }
}
