package com.se.invigilation.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class DingtalkServiceTest {
    //@Autowired
    //private DingtalkService dingtalkService;


    @Test
    void addCalander() {
        log.debug("{}", LocalDateTime.now().toString());
    }
}