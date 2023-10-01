package com.se.invigilation.component;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class DingtalkComponentTest {
    //@Autowired
    private DingtalkComponent dingtalkComponent;

    @Test
    void getDingtalkToken() {
        // String dingtalkToken = dingtalkComponent.getDingtalkToken();
        log.debug("{}", LocalTime.now().getNano());
    }
}