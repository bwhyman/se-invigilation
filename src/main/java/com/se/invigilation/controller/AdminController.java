package com.se.invigilation.controller;

import com.se.invigilation.dox.Department;
import com.se.invigilation.dox.Setting;
import com.se.invigilation.dox.User;
import com.se.invigilation.dto.UserDTO;
import com.se.invigilation.service.AdminService;
import com.se.invigilation.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/")
@Slf4j
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    //
    @PostMapping("colleges")
    public Mono<ResultVO> postColleges(@RequestBody Department department) {
        return adminService.addCollege(department).map(dep -> ResultVO.success(Map.of("department", dep)));
    }

    //
    @GetMapping("colleges")
    public Mono<ResultVO> getColleges() {
        return adminService.listColleges()
                .map(colleges -> ResultVO.success(Map.of("colleges", colleges)));
    }

    @PostMapping("users")
    public Mono<ResultVO> postDepartments(@RequestBody UserDTO userDTO) {

        return adminService.addUsers(userDTO.getCollId(), userDTO.getCollegeName(), userDTO.getUsers()).map(us -> ResultVO.success(Map.of()));
    }

    //
    @PostMapping("settings")
    public Mono<ResultVO> posSettings(@RequestBody Setting setting) {
        return adminService.addSetting(setting).map(s -> {
            return ResultVO.success(Map.of("setting", s));
        });
    }

    @PostMapping("dingusers")
    public Mono<ResultVO> postUsers(@RequestBody List<User> users) {
        return adminService.updateUsersDing(users)
                .thenReturn(ResultVO.success(Map.of()));
    }
}
