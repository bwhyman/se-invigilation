package com.se.invigilation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeRemarkDTO {
    private String dingUserIds;
    private List<String> inviIds;
    private String remark;
}
