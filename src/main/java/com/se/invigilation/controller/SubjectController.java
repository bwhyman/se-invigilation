package com.se.invigilation.controller;

import com.se.invigilation.dox.User;
import com.se.invigilation.dto.AssignUserDTO;
import com.se.invigilation.dto.NoticeDTO;
import com.se.invigilation.service.DingtalkService;
import com.se.invigilation.service.SubjectService;
import com.se.invigilation.vo.RequestConstant;
import com.se.invigilation.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subject/")
@Slf4j
@RequiredArgsConstructor
public class SubjectController {
    private final SubjectService subjectService;
    private final DingtalkService dingtalkService;

    // 获取部门内全部教师
    @GetMapping("users")
    public Mono<ResultVO> getUsers(@RequestAttribute(RequestConstant.DEPID) String depid) {
        return subjectService.listUsers(depid)
                .map(users -> ResultVO.success(Map.of("users", users)));
    }

    // 获取指定状态，指定页，监考
    @GetMapping("invis/status/{status}/{page}")
    public Mono<ResultVO> getInvis(@RequestAttribute(RequestConstant.DEPID) String depid,
                                   @PathVariable int status, @PathVariable int page) {
        Pageable pageable = PageRequest.of(page - 1, RequestConstant.pageSize);
        return subjectService.listInvigilations(depid, status, pageable).map((invis) ->
                ResultVO.success(Map.of("invis", invis)));
    }
    // 获取指定状态监考数量
    @GetMapping("/invis/status/{status}/total")
    public Mono<ResultVO> getInvisStatusTotal(@RequestAttribute(RequestConstant.DEPID) String depid, @PathVariable int status) {
        return subjectService.getInvisTotal(depid, status).map((total) ->
                ResultVO.success(Map.of("total", total)));
    }

    // 更新教师监考状态
    @PostMapping("invistatus")
    public Mono<ResultVO> postInviStatus(@RequestBody List<User> users,
                                         @RequestAttribute(RequestConstant.DEPID) String depid) {
        return subjectService.updateUserInviStatus(users).flatMap((r) ->
                subjectService.listUsers(depid).map((users1) ->
                        ResultVO.success(Map.of("users", users1))));
    }
    // 获取指定周/星期的全部课表
    @GetMapping("timetables/weeks/{week}/dayweeks/{dayweek}")
    public Mono<ResultVO> getTimetables(@PathVariable int week,
                                        @PathVariable int dayweek,
                                        @RequestAttribute(RequestConstant.DEPID) String depid) {
        return subjectService.listTimetable(depid, week, dayweek).map((timetables) ->
                ResultVO.success(Map.of("timetables", timetables)));
    }
    // 获取部门教师监考数量
    @GetMapping("invidetails/counts")
    public Mono<ResultVO> getCounts(@RequestAttribute(RequestConstant.DEPID) String depid) {
        return subjectService.listUserCounts(depid).map((counts) ->
                ResultVO.success(Map.of("counts", counts)));
    }
    // 获取指定日期全部监考
    @GetMapping("invis/dates/{date}")
    public Mono<ResultVO> getDateInvis(@RequestAttribute(RequestConstant.DEPID) String depid,
                                       @PathVariable LocalDate date) {
        return subjectService.listInvigilations(depid, date).map((invigilations) ->
                ResultVO.success(Map.of("invis", invigilations)));
    }

    // 取消监原考通知；删除钉钉日程ID；删除全监考详细信息；创建新监考详细信息
    @PostMapping("invidetails/{inviid}")
    public Mono<ResultVO> postInviDetails(@PathVariable String inviid,
                                          @RequestAttribute(RequestConstant.DEPID) String depid,
                                          @RequestBody AssignUserDTO assignUser) {
        return dingtalkService.cancel(inviid).flatMap((r) ->
                subjectService.updateInviCalanderNull(inviid)).flatMap((r) ->
                subjectService.addInvidetails(inviid, depid, assignUser).map((re) ->
                        ResultVO.success(Map.of())));
    }
    // 获取指定监考教师信息，及钉钉信息
    @GetMapping("invidetailusers/{inviid}")
    public Mono<ResultVO> getInviUsers(@PathVariable String inviid) {
        return subjectService.listInviDetailUsers(inviid)
                .map(users -> ResultVO.success(Map.of("users", users)));
    }
    // 发送钉钉监考通知，监考日程
    @PostMapping("assignnotices")
    public Mono<ResultVO> postAssignNotices(@RequestBody NoticeDTO notice) {
        return dingtalkService.noticeAssigners(notice.getUserIds(), notice.getNoticeMessage())
                .flatMap(result ->
                        dingtalkService.addCalander(notice.getCreateUnionId(),
                                        notice.getDate(),
                                        notice.getStime(),
                                        notice.getEtime(),
                                        notice.getUnionIds(),
                                        notice.getNoticeMessage())
                                .flatMap(code -> subjectService.updateInviCalanderId(notice.getInviId(), code, notice.getCreateUnionId())
                                        .thenReturn(ResultVO.success(Map.of("code", code))))
                );
    }
}
