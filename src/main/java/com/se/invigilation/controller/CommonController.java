package com.se.invigilation.controller;

import com.se.invigilation.dox.User;
import com.se.invigilation.service.CommonService;
import com.se.invigilation.vo.RequestConstant;
import com.se.invigilation.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/")
@Slf4j
@RequiredArgsConstructor
public class CommonController {
    private final CommonService commonService;

    @GetMapping("settings")
    @Cacheable(value = "settings")
    public Mono<ResultVO> getSetting() {
        return commonService.getSettings()
                .map(settings -> ResultVO.success(Map.of("settings", settings)));
    }

    @PostMapping("passwords")
    public Mono<ResultVO> postPassword(@RequestBody User user, @RequestAttribute(RequestConstant.UID) String uid) {
        return commonService.updatePassword(uid, user.getPassword())
                .thenReturn(ResultVO.success(Map.of()));
    }

}
