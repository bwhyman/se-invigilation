package com.se.invigilation.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.se.invigilation.component.JWTComponent;
import com.se.invigilation.dox.User;
import com.se.invigilation.dto.DepartmentDTO;
import com.se.invigilation.exception.Code;
import com.se.invigilation.service.UserService;
import com.se.invigilation.vo.RequestConstant;
import com.se.invigilation.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/")
@Slf4j
@RequiredArgsConstructor
public class LoginController {
    private final UserService userService;
    private final PasswordEncoder encoder;
    private final JWTComponent jwtComponent;
    private final ObjectMapper objectMapper;

    @PostMapping("login")
    public Mono<ResultVO> login(@RequestBody User user, ServerHttpResponse response) {
        return userService.getUser(user.getAccount())
                .filter(u -> encoder.matches(user.getPassword(), u.getPassword()))
                .map(u -> {
                    DepartmentDTO dd = null;
                    try {
                        dd = objectMapper.readValue(u.getDepartment(), DepartmentDTO.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                    Map<String, Object> tokenM = Map.of(
                            RequestConstant.UID, u.getId(),
                            RequestConstant.ROLE, u.getRole(),
                            RequestConstant.COLLID, dd.getCollId(),
                            RequestConstant.DEPID, dd.getDepId()
                    );
                    String token = jwtComponent.encode(tokenM);
                    response.getHeaders().add(RequestConstant.TOKEN, token);
                    response.getHeaders().add(RequestConstant.ROLE, u.getRole());
                    return ResultVO.success(Map.of("user", u));
                })
                .defaultIfEmpty(ResultVO.error(Code.LOGIN_ERROR));
    }
}