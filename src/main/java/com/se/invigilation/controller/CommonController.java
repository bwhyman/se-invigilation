package com.se.invigilation.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.se.invigilation.component.JWTComponent;
import com.se.invigilation.component.LTokenComponent;
import com.se.invigilation.dox.Invigilation;
import com.se.invigilation.dox.User;
import com.se.invigilation.dto.DepartmentDTO;
import com.se.invigilation.exception.Code;
import com.se.invigilation.service.CollegeService;
import com.se.invigilation.service.CommonService;
import com.se.invigilation.service.SubjectService;
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
    private final LTokenComponent lTokenComponent;
    private final CollegeService collegeService;
    private final SubjectService subjectService;

    @PostMapping("login")
    public Mono<ResultVO> login(@RequestBody Map<String, String> map, ServerHttpResponse response) {
        String account = map.get("account");
        String password = map.get("password");
        String ltoken = map.get("ltoken");
        return commonService.getUser(account).filter((u) ->
                encoder.matches(password, u.getPassword())).map((u) -> {
            try {
                DepartmentDTO dd = objectMapper.readValue(u.getDepartment(), DepartmentDTO.class);
                Map<String, Object> tokenM = Map.of(RequestConstant.UID, u.getId(),
                        RequestConstant.ROLE, u.getRole(),
                        RequestConstant.COLLID, dd.getCollId(),
                        RequestConstant.DEPID, dd.getDepId());
                String token = jwtComponent.encode(tokenM);
                response.getHeaders().add("token", token);
                response.getHeaders().add("role", u.getRole());
                if (ltoken != null) {
                    response.getHeaders().add("ltoken", lTokenComponent.encode(u.getAccount()));
                }
                return ResultVO.success(Map.of("user", u));
            } catch (JsonProcessingException var6) {
                return ResultVO.error(Code.LOGIN_ERROR);
            }
        }).defaultIfEmpty(ResultVO.error(Code.LOGIN_ERROR));
    }

    @GetMapping("l-login")
    public Mono<ResultVO> login(@RequestHeader(name = "ltoken", required = false) String ltoken,
                                ServerHttpResponse response) {
        if (ltoken == null) {
            return Mono.just(ResultVO.error(Code.LOGIN_TOKEN_ERROR));
        }
        return lTokenComponent.decode(ltoken)
                .flatMap(account -> commonService.getUser(account).map(u -> {
                    try {
                        DepartmentDTO dd = objectMapper.readValue(u.getDepartment(), DepartmentDTO.class);
                        Map<String, Object> tokenM = Map.of(RequestConstant.UID, u.getId(),
                                RequestConstant.ROLE, u.getRole(),
                                RequestConstant.COLLID, dd.getCollId(),
                                RequestConstant.DEPID, dd.getDepId());
                        String token = jwtComponent.encode(tokenM);
                        response.getHeaders().add("token", token);
                        response.getHeaders().add("role", u.getRole());
                        return ResultVO.success(Map.of("user", u));
                    } catch (JsonProcessingException var6) {
                        return ResultVO.error(Code.LOGIN_ERROR);
                    }
                }));

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
                thenReturn(ResultVO.ok());
    }

    @GetMapping("users/{account}")
    public Mono<ResultVO> getUser(@PathVariable String account) {
        return commonService.getUser(account)
                .map(user -> ResultVO.success(Map.of("user", user)));
    }

    // 获取指定全部用户的DING IDS。虽然是获取，但通过post传递参数较方便
    @PostMapping("invinotices/dingids")
    public Mono<ResultVO> postUserIds(@RequestBody List<String> ids) {
        log.debug("{}", ids);
        return commonService.listUsersDingIds(ids)
                .map(users -> ResultVO.success(Map.of("users", users)));
    }

    @PostMapping("cancelinvinotices/{inviid}")
    public Mono<ResultVO> deleteInvigilation(
            @RequestBody Map<String, String> map,
            @PathVariable String inviid,
            @RequestAttribute(RequestConstant.ROLE) String role,
            @RequestAttribute(RequestConstant.DEPID) String depid,
            @RequestAttribute(RequestConstant.COLLID) String collid) {
        Mono<Invigilation> invigilationMono;
        if (role.equals(User.COLLEGE_ADMIN)) {
            invigilationMono = collegeService.getInvigilation(collid, inviid);
        } else if (role.equals(User.SUBJECT_ADMIN)) {
            invigilationMono = subjectService.getInvigilation(depid, inviid);
        } else {
            invigilationMono = Mono.empty();
        }
        return invigilationMono
                .flatMap(invi -> {
                    if (invi.getCalendarId() == null) {
                        return Mono.just(ResultVO.ok("未设置日历"));
                    }
                    return commonService.listUserDingIdsByInviid(inviid)
                            .map(users -> users.stream().map(User::getDingUserId).toList())
                            .flatMap(dingUserIds -> {
                                if (dingUserIds.isEmpty()) {
                                    return Mono.just(ResultVO.ok("未通知用户"));
                                }
                                var dingsString = String.join(",", dingUserIds);
                                var cancelMessage = map.get("cancelMessage");
                                return commonService.cancelInvigilation(invi, dingsString, cancelMessage)
                                        .thenReturn(ResultVO.ok());
                            });
                })
                .defaultIfEmpty(ResultVO.error(Code.ERROR, "监考获取错误"));
    }

    // 加载指定日期内全部监考
    @GetMapping("invis/date/{sdate}/{edate}")
    public Mono<ResultVO> getinvisDate(
            @PathVariable String sdate,
            @PathVariable String edate,
            @RequestAttribute(RequestConstant.ROLE) String role,
            @RequestAttribute(RequestConstant.DEPID) String depid,
            @RequestAttribute(RequestConstant.COLLID) String collid) {
        Mono<List<Invigilation>> invigilationsMono;
        if(User.COLLEGE_ADMIN.equals(role)) {
            invigilationsMono = collegeService.listInvisByDateByCollId(collid, sdate, edate);
        } else if(User.SUBJECT_ADMIN.equals(role)) {
            invigilationsMono = subjectService.listInvisByDateByDepId(depid, sdate, edate);
        }else {
            invigilationsMono = Mono.empty();
        }
        return invigilationsMono.map(invis -> ResultVO.success(Map.of("invis", invis)));
    }

}
