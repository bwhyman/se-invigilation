package com.se.invigilation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentAvgDTO {
    private String depId;
    private Integer departmentQuantity;
    private Integer teacherQuantity;
}
