package com.se.invigilation.dox;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Department {
    public static final int OPEN = 1;
    public static final int CLOSED = 0;
    public static final int ROOT = 1;
    @Id
    @CreatedBy
    private String id;
    private String name;
    private String college;
    private Integer root;
    private Integer inviStatus;
    private String dingDepid;
    private String comment;
    @ReadOnlyProperty
    @JsonIgnore
    private LocalDateTime insertTime;
    @ReadOnlyProperty
    @JsonIgnore
    private LocalDateTime updateTime;
}
