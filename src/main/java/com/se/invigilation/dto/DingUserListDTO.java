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
public class DingUserListDTO {
    private String errcode;
    private Result result;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Result {
        private Integer next_cursor;
        private Boolean has_more;
        private List<DingUser> list;
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DingUser {
        private String unionid;
        private String userid;
        private String name;
        private String mobile;
    }
}
