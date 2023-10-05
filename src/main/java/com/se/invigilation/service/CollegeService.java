package com.se.invigilation.service;

import com.se.invigilation.dox.Department;
import com.se.invigilation.dox.Invigilation;
import com.se.invigilation.dox.Timetable;
import com.se.invigilation.dox.User;
import com.se.invigilation.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
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

    public Mono<Integer> getdispatchedTotal(String depid) {
        return invigilationRepository.findIDispatchedTotal(depid);
    }

    public Mono<List<Invigilation>> listDispatchedInvis(String depid, Pageable pageable) {
        return invigilationRepository.findDispatcheds(depid, pageable).collectList();
    }

    @Transactional
    public Mono<List<Integer>> updateDispatcher(List<Invigilation> invigilations) {
        List<Mono<Integer>> monos = new ArrayList<>();
        for (Invigilation invigilation : invigilations) {
            Mono<Integer> update = invigilationRepository.findById(invigilation.getId())
                    .flatMap((invi) -> {
                        invi.setDepartment(invigilation.getDepartment());
                        invi.setStatus(Invigilation.DISPATCH);
                        invi.setDispatcher(invigilation.getDispatcher());
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

    @Transactional
    public Mono<Integer> addTimetable(String userid, List<Timetable> timetables) {
        return timetableRepository.deleteByUserId(userid).flatMap((r) ->
            timetableRepository.saveAll(timetables).collectList()
                    .thenReturn(1));
    }

    @Transactional
    public Mono<Integer> removeInvigilation(String inviid) {
        return inviDetailRepository.deleteByInviId(inviid).flatMap((r) ->
            invigilationRepository.deleteById(inviid).thenReturn(1));
    }

    @Transactional
    public Mono<Integer> updateInvigilation(Invigilation invigilation) {
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
            invi.setDepartment(null);
            invi.setDispatcher(null);
            invi.setAllocator(null);
            invi.setExecutor(null);
            invi.setCalendarId(null);
            invi.setCreateUnionId(null);
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
}
