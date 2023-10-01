package com.se.invigilation.controller;

import com.se.invigilation.dox.Invigilation;
import com.se.invigilation.dox.Timetable;
import com.se.invigilation.dox.User;
import com.se.invigilation.service.CollegeService;
import com.se.invigilation.service.DingtalkService;
import com.se.invigilation.vo.RequestConstant;
import com.se.invigilation.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/college/")
@Slf4j
@RequiredArgsConstructor
public class CollegeController {
    private final CollegeService collegeService;
    private final DingtalkService dingtalkService;

    @PostMapping("invigilations")
    public Mono<ResultVO> postInvigilations(@RequestBody List<Invigilation> invigilations) {
        return collegeService.addInvigilations(invigilations)
                .thenReturn(ResultVO.success(Map.of()));
    }

    @GetMapping("departments")
    public Mono<ResultVO> getDepartments(@RequestAttribute(RequestConstant.COLLID) String collid) {
        return collegeService.listDepartments(collid)
                .map(deps -> ResultVO.success(Map.of("departments", deps)));
    }

    @GetMapping("invigilations/status/{status}")
    public Mono<ResultVO> getInvis(@RequestAttribute(RequestConstant.COLLID) String collid, @PathVariable int status) {
        return collegeService.listCollegeInvigilations(collid, status)
                .map(invis -> ResultVO.success(Map.of("invis", invis)));
    }

    @PatchMapping("invigilations")
    public Mono<ResultVO> patchInvigilations(@RequestAttribute(RequestConstant.COLLID) String collid, @RequestBody List<Invigilation> invigilations) {
        return collegeService.updateDispatcher(invigilations)
                .flatMap(result -> collegeService.listCollegeInvigilations(collid, Invigilation.IMPORT)
                        .map(invis -> ResultVO.success(Map.of("invis", invis))));
    }

    @GetMapping("users")
    public Mono<ResultVO> getUsers(@RequestAttribute(RequestConstant.COLLID) String collid) {
        return collegeService.listUser(collid)
                .map(users -> ResultVO.success(Map.of("users", users)));
    }
    @PostMapping("timetables")
    public Mono<ResultVO> postTimetables(@RequestBody List<Timetable> timetables) {
        return collegeService.addTimetables(timetables)
                .thenReturn(ResultVO.success(Map.of()));
    }

    @GetMapping("dispatchers/{depid}")
    public Mono<ResultVO> getSubjectDispatchers(@PathVariable String depid) {
        return collegeService.listUsers(depid, User.SUBJECT_ADMIN)
                .map(users -> ResultVO.success(Map.of("users", users)));
    }

    @PostMapping("dispatchnotices")
    public Mono<ResultVO> postDispatchNotice(@RequestBody String userIds) {
        return dingtalkService.noticeDispatchers(userIds)
                .map(result -> ResultVO.success(Map.of("dingResp", result)))
                .onErrorResume(e -> Mono.just(ResultVO.error(400, e.getMessage())));
    }

    @PostMapping("invigilation")
    public Mono<ResultVO> postInvigilation(@RequestBody Invigilation invigilation) {
        return collegeService.addInvigilation(invigilation)
                .thenReturn(ResultVO.success(Map.of()));
    }
}
