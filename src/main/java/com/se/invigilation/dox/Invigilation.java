package com.se.invigilation.dox;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Invigilation {
    public static final int IMPORT = 0;
    public static final int DISPATCH = 1;
    public static final int ASSIGN = 2;
    public static final int DONE = 5;

    @Id
    @CreatedBy
    private String id;
    private String collId;
    private String department;
    private String importer;
    private String dispatcher;
    private String allocator;
    private String executor;
    private LocalDate date;
    private String time;
    private String course;
    private Integer amount;
    private Integer status;
    private String createUnionId;
    private String calendarId;

    @ReadOnlyProperty
    private LocalDateTime insertTime;
    @ReadOnlyProperty
    private LocalDateTime updateTime;
}
