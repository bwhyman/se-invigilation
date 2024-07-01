package com.se.invigilation.component;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
class LTokenComponentTest {
    @Autowired
    private LTokenComponent lTokenComponent;

    @Test
    void test() {
        String test = "1020090008";
        log.debug(test);
        String encode = lTokenComponent.encode(test);
        log.debug(encode);
        lTokenComponent.decode(encode).doOnSuccess(log::debug).block();
        //log.debug(decode);
    }
}