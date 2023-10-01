package com.se.invigilation.repository;

import com.se.invigilation.dox.Timetable;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class TimetableRepositoryTest {
    @Autowired
    private TimetableRepository timetableRepository;
    @Test
    void findByDepIdAndDate() {
        timetableRepository.findByDepIdAndDate("1154987556587593728", 1, 14)
                .collectList()
                .blockOptional()
                .ifPresent(timetables -> {
                    for (Timetable timetable : timetables) {
                        log.debug(timetable.getTeacherName());
                    }
                });
    }
}