package com.se.invigilation.controller;

import com.se.invigilation.dox.Department;
import com.se.invigilation.dox.Setting;
import com.se.invigilation.dto.UserDTO;
import com.se.invigilation.service.AdminService;
import com.se.invigilation.service.DingtalkService;
import com.se.invigilation.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

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
                .flatMap(r -> adminService.listColleges()
                        .map(colleges -> ResultVO.success(Map.of("colleges", colleges))));
    }

    //
    @GetMapping("colleges")
    public Mono<ResultVO> getColleges() {
        return adminService.listColleges()
                .map(colleges -> ResultVO.success(Map.of("colleges", colleges)));
    }

    @PostMapping("users")
    public Mono<ResultVO> postDepartments(@RequestBody UserDTO userDTO) {

        return adminService.addUsers(userDTO.getCollId(), userDTO.getCollegeName(), userDTO.getUsers()).map(us -> ResultVO.ok());
    }

    //
    @PostMapping("settings")
    public Mono<ResultVO> posSettings(@RequestBody Setting setting) {
        return adminService.addSetting(setting).map(s ->
            ResultVO.success(Map.of("setting", s)));
    }

    // 获取学院教师钉钉信息
    @GetMapping("dingusers/{dingdepid}")
    public Mono<ResultVO> getDingUsers(@PathVariable long dingdepid) {
        return dingtalkService.listDingUsers(dingdepid)
                .map(dingUsers -> ResultVO.success(Map.of("users", dingUsers)));
    }
}
