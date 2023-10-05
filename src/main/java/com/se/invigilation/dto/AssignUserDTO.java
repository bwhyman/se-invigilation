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
    private String allocator;
    private List<AssignUser> executor;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    public static class AssignUser {
        private String userId;
        private String userName;
        private String time;
    }

}
