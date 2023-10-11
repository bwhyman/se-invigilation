package com.se.invigilation.repository;

import com.se.invigilation.dox.Timetable;
import com.se.invigilation.dox.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class TimetableRepositoryTest {
    @Autowired
    private TimetableRepository timetableRepository;


}