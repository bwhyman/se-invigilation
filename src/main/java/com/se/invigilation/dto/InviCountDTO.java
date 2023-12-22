package com.se.invigilation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class InviCountDTO {
    private String userId;
    private String account;
    private Integer count;
    private String name;
    private String departmentName;
}
