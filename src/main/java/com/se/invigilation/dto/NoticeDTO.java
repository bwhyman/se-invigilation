package com.se.invigilation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class NoticeDTO {
    private String inviId;
    private String createUnionId;
    private String date;
    private String stime;
    private String etime;
    private List<String> unionIds;
    private String noticeMessage;
    // 记录在invigilation表的用户通知数组
    private String noticeUserIds;
    // dinguserids
    private String userIds;
}
