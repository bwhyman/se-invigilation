package com.se.invigilation.dox;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class User {
    public static final String USER = "pL6sC";
    public static final String SUBJECT_ADMIN = "pO8vE";
    public static final String COLLEGE_ADMIN = "sfLN4";
    public static final String SUPER_ADMIN = "UrO7n";
    public static final int INVI_STATUS_CLOSE = 0;
    public static final int INVI_STATUS_OPEN = 1;

    @Id
    @CreatedBy
    private String id;
    private String name;
    private String account;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private String description;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String role;
    private String mobile;
    private String dingUnionId;
    private String dingUserId;
    private Integer inviStatus;
    private String department;
    @ReadOnlyProperty
    private LocalDateTime insertTime;
    @ReadOnlyProperty
    private LocalDateTime updateTime;
}
