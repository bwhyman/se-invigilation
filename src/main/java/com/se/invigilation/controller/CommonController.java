package com.se.invigilation.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.se.invigilation.component.JWTComponent;
import com.se.invigilation.dox.User;
import com.se.invigilation.dto.DepartmentDTO;
import com.se.invigilation.exception.Code;
import com.se.invigilation.service.CommonService;
import com.se.invigilation.service.DingtalkService;
import com.se.invigilation.vo.RequestConstant;
import com.se.invigilation.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/")
@Slf4j
@RequiredArgsConstructor
public class CommonController {
    private final CommonService commonService;
    private final PasswordEncoder encoder;
    private final JWTComponent jwtComponent;
    private final ObjectMapper objectMapper;
    private final DingtalkService dingtalkService;

    @PostMapping("login")
    public Mono<ResultVO> login(@RequestBody User user, ServerHttpResponse response) {
        return commonService.getUser(user.getAccount()).filter((u) ->
            encoder.matches(user.getPassword(), u.getPassword())).map((u) -> {
            try {
                DepartmentDTO dd = objectMapper.readValue(u.getDepartment(), DepartmentDTO.class);
                Map<String, Object> tokenM = Map.of(RequestConstant.UID, u.getId(),
                        RequestConstant.ROLE, u.getRole(),
                        RequestConstant.COLLID, dd.getCollId(),
                        RequestConstant.DEPID, dd.getDepId());
                String token = this.jwtComponent.encode(tokenM);
                response.getHeaders().add("token", token);
                response.getHeaders().add("role", u.getRole());
                return ResultVO.success(Map.of("user", u));
            } catch (JsonProcessingException var6) {
                return ResultVO.error(Code.LOGIN_ERROR);
            }
        }).defaultIfEmpty(ResultVO.error(Code.LOGIN_ERROR));
    }

    @GetMapping("settings")
    public Mono<ResultVO> getSetting() {
        return commonService.getSettings().map((settings) ->
            ResultVO.success(Map.of("settings", settings)));
    }

    @PostMapping("passwords")
    public Mono<ResultVO> postPassword(@RequestBody User user,
                                       @RequestAttribute(RequestConstant.UID) String uid) {
        return commonService.updatePassword(uid, user.getPassword()).
                thenReturn(ResultVO.success(Map.of()));
    }

    @GetMapping("users/{account}")
    public Mono<ResultVO> getUser(@PathVariable String account) {
        return commonService.getUser(account)
                .map(user -> ResultVO.success(Map.of("user", user)));
    }

    // 获取指定全部用户的DING IDS。虽然是获取，但通过post传递参数较方便
    @PostMapping("invinotices/dingids")
    public Mono<ResultVO> postUserIds(@RequestBody List<String> ids) {
        return commonService.listUsersDingIds(ids)
                .map(users -> ResultVO.success(Map.of("users", users)));
    }

    // 发送监考取消通知，删除钉钉日程
    @DeleteMapping("invinotices/{inviid}")
    public Mono<ResultVO> deleteInvigilation(@PathVariable String inviid) {
        return dingtalkService.cancel(inviid).
                thenReturn(ResultVO.success(Map.of()));
    }
}
