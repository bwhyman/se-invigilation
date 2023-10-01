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
public class AssignUserDTO {
    private String inviId;
    private String userIds;
    private String depId;
    private String allocator;
    private List<AssignUser> executor;
    private String oldDingUserIds;
    private String cancelMessage;
    private String calendarId;
    private String createUnionId;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    public static class AssignUser {
        private String unionId;
        private String userId;
        private String userName;
        private String time;
    }

}
