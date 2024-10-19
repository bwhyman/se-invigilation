package com.se.invigilation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AssignUserDTO {
    private String department;
    private String allocator;
    private String executor;
    private String dispatcher;
    private Integer amount;
    private String[] userIds;
}
