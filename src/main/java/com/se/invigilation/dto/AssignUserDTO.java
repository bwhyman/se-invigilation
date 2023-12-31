package com.se.invigilation.dto;

import com.se.invigilation.dox.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AssignUserDTO {
    private String department;
    private String allocator;
    private List<User> users;
    private String executor;
}
