package com.se.invigilation.service;

import com.se.invigilation.dox.Timetable;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class SubjectServiceTest {
    @Autowired
    private SubjectService subjectService;

    @Test
    void listTimetable() {
        subjectService.listTimetable("1154987556587593728", 1, 14)
                .blockOptional()
                .ifPresent(timetables -> {
                    for (Timetable timetable : timetables) {
                        log.debug(timetable.getTeacherName());
                    }
                });
    }
}