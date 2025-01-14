package com.se.invigilation.controller;

import com.se.invigilation.dox.Department;
import com.se.invigilation.dox.Setting;
import com.se.invigilation.dox.User;
import com.se.invigilation.dto.UserDTO;
import com.se.invigilation.service.AdminService;
import com.se.invigilation.service.DingtalkService;
import com.se.invigilation.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/admin/")
@Slf4j
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    private final DingtalkService dingtalkService;

    //
    @PostMapping("colleges")
    public Mono<ResultVO> postColleges(@RequestBody Department department) {
        return adminService.addCollege(department)
                .then(adminService.listColleges())
                .map(ResultVO::success);
    }

    //
    @GetMapping("colleges")
    public Mono<ResultVO> getColleges() {
        return adminService.listColleges()
                .map(ResultVO::success);
    }

    @PostMapping("users")
    public Mono<ResultVO> postUsers(@RequestBody UserDTO userDTO) {
        return adminService.addUsers(userDTO.getCollId(), userDTO.getCollegeName(), userDTO.getUsers())
                .thenReturn(ResultVO.ok());
    }

    //
    @PostMapping("settings")
    public Mono<ResultVO> postSettings(@RequestBody Setting setting) {
        return adminService.addSetting(setting)
                .then(adminService.listSettings())
                .map(ResultVO::success);
    }

    //
    @PatchMapping("settings")
    public Mono<ResultVO> patchSettings(@RequestBody Setting setting) {
        return adminService.updateSetting(setting)
                .then(adminService.listSettings())
                .map(ResultVO::success);
    }

    // 获取学院教师钉钉信息
    @GetMapping("dingusers/{dingdepid}")
    public Mono<ResultVO> getDingUsers(@PathVariable long dingdepid) {
        return dingtalkService.listDingUsers(dingdepid)
                .map(ResultVO::success);
    }

    //
    @GetMapping("colleges/{collid}/users")
    public Mono<ResultVO> getCollegeUsers(@PathVariable String collid) {
        return adminService.listCollegeUsers(collid)
                .map(ResultVO::success);
    }

    // 导入指定学院账号的钉钉信息，部分可能为空，后期单独更新
    @PostMapping("colleges/{collid}/userdings")
    public Mono<ResultVO> postUserdings(@PathVariable String collid,
                                        @RequestBody List<User> users) {
        return adminService.updateCollUsersDing(users, collid)
                .thenReturn(ResultVO.ok());
    }
}
