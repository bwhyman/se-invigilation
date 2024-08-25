package com.se.invigilation.service;

import com.se.invigilation.dox.Department;
import com.se.invigilation.dox.Setting;
import com.se.invigilation.dox.User;
import com.se.invigilation.exception.Code;
import com.se.invigilation.exception.XException;
import com.se.invigilation.repository.DepartmentRepository;
import com.se.invigilation.repository.SettingRepository;
import com.se.invigilation.repository.UserRepository;
import io.r2dbc.spi.Statement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
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
    private final DatabaseClient databaseClient;

    @Transactional
    public Mono<Integer> updateSetting(Setting setting) {
        return settingRepository.update(setting.getId(), setting.getSvalue());
    }

    @Transactional
    public Mono<Setting> addSetting(Setting setting) {
        return settingRepository.save(setting);
    }

    @Transactional
    public Mono<Department> addCollege(Department department) {
        return departmentRepository.save(department);
    }

    //
    public Mono<List<Department>> listColleges() {
        return departmentRepository.findByRoot(Department.ROOT).collectList();
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
                    .inviStatus(1)
                    .build();
            departs.add(department);
        }
        List<Mono<Department>> monos = new ArrayList<>();
        for (Department department : departs) {
            Mono<Department> departmentMono = departmentRepository.findByCollIdAndName(collId, department.getName())
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

    @Transactional
    public Mono<Integer> updateCollUsersDing(List<User> users, String collid) {
        var sql = """
                update user u set u.ding_user_id=?, u.ding_union_id=?
                where u.account=? and u.department ->> '$.collId'=?
                """;
        return databaseClient.inConnection(conn -> {
            Statement statement = conn.createStatement(sql);
            for (int i = 0; i < users.size(); i++) {
                statement.bindNull(0, String.class)
                        .bindNull(1, String.class)
                        .bind(2, users.get(i).getAccount())
                        .bind(3, collid);
                if (users.get(i).getDingUnionId() != null && users.get(i).getDingUserId() != null) {
                    statement.bind(0, users.get(i).getDingUserId())
                            .bind(1, users.get(i).getDingUnionId());
                }
                if (i < users.size() - 1) {
                    statement.add();
                }
            }
            return Flux.from(statement.execute()).collectList();
        }).thenReturn(1);
    }

    public Mono<List<User>> listCollegeUsers(String collid) {
        return userRepository.findByCollId(collid).collectList();
    }
}

