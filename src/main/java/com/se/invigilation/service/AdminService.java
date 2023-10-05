package com.se.invigilation.service;

import com.se.invigilation.dox.Department;
import com.se.invigilation.dox.Setting;
import com.se.invigilation.dox.User;
import com.se.invigilation.exception.XException;
import com.se.invigilation.repository.DepartmentRepository;
import com.se.invigilation.repository.SettingRepository;
import com.se.invigilation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final SettingRepository settingRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @CacheEvict(value = "settings", allEntries = true)
    public Mono<Integer> updateSetting(String sid, LocalDate startWeek) {
        return settingRepository.update(sid, startWeek.toString());
    }

    @Transactional
    @CacheEvict(value = "settings", allEntries = true)
    public Mono<Setting> addSetting(Setting setting) {
        return settingRepository.save(setting);
    }

    @Transactional
    public Mono<Department> addCollege(Department department) {
        return departmentRepository.findByName(department.getName())
                .filter(dep -> {
                    throw XException.builder()
                            .codeN(400)
                            .message(department.getName() + "已存在")
                            .build();
                })
                .switchIfEmpty(Mono.defer(() -> departmentRepository.save(department)));
    }

    //
    public Mono<List<Department>> listColleges() {
        return departmentRepository.findByCollegeIsNull().collectList();
    }

    @CacheEvict(value = "users",  allEntries = true)
    @Transactional
    public Mono<List<User>> addUsers(String collId, String collegeName, List<User> userDTOS) {
        Set<String> departments = userDTOS.stream()
                .map(User::getDepartment)
                .collect(Collectors.toSet());

        List<Department> departs = new ArrayList<>();
        for (String departName : departments) {
            Department department = Department.builder()
                    .college("""
                            {"collId": "%s", "collegeName":  "%s"}
                            """.formatted(collId, collegeName))
                    .name(departName)
                    .build();
            departs.add(department);
        }
        return departmentRepository.saveAll(departs).collectList()
                .flatMap(deps -> {
                    List<User> users = new ArrayList<>();
                    for (User userDTO : userDTOS) {
                        Department department = deps.stream()
                                .filter(dep -> dep.getName().equals(userDTO.getDepartment()))
                                .findFirst().get();

                        User user = User.builder()
                                .name(userDTO.getName())
                                .inviStatus(User.INVI_STATUS_OPEN)
                                .account(userDTO.getAccount())
                                .password(passwordEncoder.encode(userDTO.getAccount()))
                                .mobile(userDTO.getMobile())
                                .role(userDTO.getRole())
                                .department("""
                                        {"collId": "%s", "collegeName":  "%s", "depId": "%s", "departmentName": "%s"}
                                        """.formatted(collId, collegeName, department.getId(), department.getName()))
                                .build();
                        users.add(user);
                    }
                    return userRepository.saveAll(users).collectList();
                });
    }

    @Transactional
    public Mono<Void> updateUsersDing(List<User> users) {
        List<Mono<Integer>> userMonos = new ArrayList<>();
        for (User user : users) {
            Mono<Integer> byName = userRepository.updateByName(user.getName(), user.getDingUnionId(), user.getDingUserId(), user.getMobile());
            userMonos.add(byName);
        }
        return Flux.merge(userMonos).then();
    }

    /*Mono<List<User>> list() {
        Pageable pageRequest = PageRequest.of(0, 20);
        return userRepository.findByCollId("1154814591036186624", pageRequest).collectList();
    }*/

}

