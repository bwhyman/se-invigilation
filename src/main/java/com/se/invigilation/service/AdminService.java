package com.se.invigilation.service;

import com.se.invigilation.dox.Department;
import com.se.invigilation.dox.Setting;
import com.se.invigilation.dox.User;
import com.se.invigilation.exception.Code;
import com.se.invigilation.exception.XException;
import com.se.invigilation.repository.DepartmentRepository;
import com.se.invigilation.repository.SettingRepository;
import com.se.invigilation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public Mono<Integer> updateSetting(String sid, LocalDate startWeek) {
        return settingRepository.update(sid, startWeek.toString());
    }

    @Transactional
    public Mono<Setting> addSetting(Setting setting) {
        return settingRepository.save(setting);
    }

    @Transactional
    public Mono<Department> addCollege(Department department) {
        return departmentRepository.findByName(department.getName())
                .filter(dep -> {
                    throw XException.builder()
                            .codeN(Code.ERROR)
                            .message(department.getName() + "已存在")
                            .build();
                })
                .switchIfEmpty(Mono.defer(() -> departmentRepository.save(department)));
    }

    //
    public Mono<List<Department>> listColleges() {
        return departmentRepository.findByCollegeIsNull().collectList();
    }

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
        List<Mono<Department>> monos = new ArrayList<>();
        for (Department department : departs) {
            Mono<Department> departmentMono = departmentRepository.findByName(department.getName())
                    .switchIfEmpty(departmentRepository.save(department));
            monos.add(departmentMono);
        }
        return Flux.merge(monos).collectList()
                .flatMap(deps -> {
                    List<User> users = new ArrayList<>();
                    for (User userDTO : userDTOS) {
                        Department department = deps.stream()
                                .filter(dep -> dep.getName().equals(userDTO.getDepartment()))
                                .findFirst().orElseThrow();

                        User user = User.builder()
                                .name(userDTO.getName())
                                .inviStatus(User.INVI_STATUS_OPEN)
                                .account(userDTO.getAccount())
                                .password(passwordEncoder.encode(userDTO.getAccount()))
                                .mobile(userDTO.getMobile())
                                .role(userDTO.getRole())
                                .dingUserId(userDTO.getDingUserId())
                                .dingUnionId(userDTO.getDingUnionId())
                                .department("""
                                        {"collId": "%s", "collegeName":  "%s", "depId": "%s", "departmentName": "%s"}
                                        """.formatted(collId, collegeName, department.getId(), department.getName()))
                                .build();
                        users.add(user);
                    }
                    return userRepository.saveAll(users).collectList();
                });
    }


}

