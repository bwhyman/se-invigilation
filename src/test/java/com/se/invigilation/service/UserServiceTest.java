package com.se.invigilation.service;

import com.se.invigilation.dox.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
class UserServiceTest {
    @Autowired
    private UserService userService;
    @Test
    void getUserByNumber() {
        User user = userService.getUser("admin").block();
        log.debug("{}", user);
    }
}