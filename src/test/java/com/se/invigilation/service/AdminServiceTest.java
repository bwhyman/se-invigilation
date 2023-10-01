package com.se.invigilation.service;

import com.se.invigilation.dox.Department;
import com.se.invigilation.dox.User;
import com.se.invigilation.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class AdminServiceTest {
    @Autowired
    private AdminService adminService;

    @Test
    void addCollege() {
        adminService.addCollege(Department.builder().name("ok").build()).block();
    }

    @Test
    void listColleges() {
        adminService.listColleges().blockOptional()
                .ifPresent(colleges -> {
                    for (Department college : colleges) {
                        log.debug(college.getName());
                    }
                });
    }

    @Test
    void addUsers() {
        String collId = "1154814591036186624";
        String collegeName = "计控学院";
        User u1 = User.builder()
                .account("1515").
                name("sss")
                .role("ewdc")
                .mobile("2121")
                .department("软件")
                .build();
        adminService.addUsers(collId, collegeName, List.of(u1)).blockOptional()
                .ifPresent(users -> {
                    for (User user : users) {
                        log.debug("{}", user);
                    }
                });
    }

}