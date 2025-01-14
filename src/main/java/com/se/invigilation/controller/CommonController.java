package com.se.invigilation.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.se.invigilation.component.JWTComponent;
import com.se.invigilation.component.LTokenComponent;
import com.se.invigilation.dox.Invigilation;
import com.se.invigilation.dox.User;
import com.se.invigilation.dto.DepartmentDTO;
import com.se.invigilation.exception.Code;
import com.se.invigilation.exception.XException;
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
import java.util.Optional;

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
        return commonService.getUser(account)
                .filter((u) -> encoder.matches(password, u.getPassword()))
                .map((u) -> {
                    setToken(response, u);
                    if (ltoken != null) {
                        response.getHeaders().add("ltoken", lTokenComponent.encode(u.getAccount()));
                    }
                    return ResultVO.success(u);
                }).defaultIfEmpty(ResultVO.error(Code.LOGIN_ERROR));
    }

    @GetMapping("l-login")
    public Mono<ResultVO> login(@RequestHeader Optional<String> ltoken,
                                ServerHttpResponse response) {
        return ltoken.map(s -> lTokenComponent.decode(s)
                        .flatMap(commonService::getUser)
                        .map(u -> {
                            setToken(response, u);
                            return ResultVO.success(u);
                        }))
                .orElseGet(() -> Mono.just(ResultVO.error(Code.LOGIN_TOKEN_ERROR)));

    }

    private void setToken(ServerHttpResponse resp, User user) {
        try {
            DepartmentDTO dd = objectMapper.readValue(user.getDepartment(), DepartmentDTO.class);
            Map<String, Object> tokenM = Map.of(RequestConstant.UID, user.getId(),
                    RequestConstant.ROLE, user.getRole(),
                    RequestConstant.COLLID, dd.getCollId(),
                    RequestConstant.DEPID, dd.getDepId());
            String token = jwtComponent.encode(tokenM);
            resp.getHeaders().add("token", token);
            resp.getHeaders().add("role", user.getRole());
        } catch (JsonProcessingException var6) {
            throw XException.builder().code(Code.LOGIN_ERROR).build();
        }
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
                .map(ResultVO::success);
    }

    // 获取指定全部用户的DING IDS。虽然是获取，但通过post传递参数较方便
    @PostMapping("invinotices/dingids")
    public Mono<ResultVO> postUserIds(@RequestBody List<String> ids) {
        return commonService.listUsersDingIds(ids).map(ResultVO::success);
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
        if (User.COLLEGE_ADMIN.equals(role)) {
            invigilationsMono = collegeService.listInvisByDateByCollId(collid, sdate, edate);
        } else if (User.SUBJECT_ADMIN.equals(role)) {
            invigilationsMono = subjectService.listInvisByDateByDepId(depid, sdate, edate);
        } else {
            invigilationsMono = Mono.empty();
        }
        return invigilationsMono.map(ResultVO::success);
    }

    // 手动添加一个监考
    @PostMapping("invigilations")
    public Mono<ResultVO> postInvigilation(@RequestBody Invigilation invigilation,
                                           @RequestAttribute(RequestConstant.COLLID) String collid) {
        invigilation.setCollId(collid);
        return commonService.addInvigilation(invigilation)
                .thenReturn(ResultVO.ok());
    }
}
