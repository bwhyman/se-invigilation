package com.se.invigilation.repository;

import com.se.invigilation.vo.RequestConstant;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@SpringBootTest
@Slf4j
class InvigilationRepositoryTest {
    @Autowired
    private InvigilationRepository invigilationRepository;

    @Test
    void findDispatcheds() {
        Pageable pageable = PageRequest.of(0, RequestConstant.pageSize);
        invigilationRepository.findDispatchedAndAssigns("1154987556667285504", "1154814591036186624", pageable)
                .collectList()
                .block()
                .forEach(System.out::println);
    }
}