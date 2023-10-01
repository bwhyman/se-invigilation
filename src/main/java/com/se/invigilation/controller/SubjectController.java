package com.se.invigilation.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.se.invigilation.dox.User;
import com.se.invigilation.dto.AssignUserDTO;
import com.se.invigilation.dto.NoticeDTO;
import com.se.invigilation.exception.XException;
import com.se.invigilation.service.DingtalkService;
import com.se.invigilation.service.SubjectService;
import com.se.invigilation.vo.RequestConstant;
import com.se.invigilation.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subject/")
@Slf4j
@RequiredArgsConstructor
public class SubjectController {
    private final SubjectService subjectService;
    private final DingtalkService dingtalkService;
    private final ObjectMapper objectMapper;

    @GetMapping("users")
    public Mono<ResultVO> getUsers(@RequestAttribute(RequestConstant.DEPID) String depid) {
        return subjectService.listUsers(depid)
                .map(users -> ResultVO.success(Map.of("users", users)));
    }

    @GetMapping("invis/status/{status}")
    public Mono<ResultVO> getInvis(@PathVariable int status,
                                   @RequestAttribute(RequestConstant.DEPID) String depid) {
        return subjectService.listInvis(depid, status)
                .map(invigilations -> ResultVO.success(Map.of("invis", invigilations)));
    }

    @PostMapping("invistatus")
    public Mono<ResultVO> postInviStatus(@RequestBody List<User> users, @RequestAttribute(RequestConstant.DEPID) String depid) {
        return subjectService.updateUserInviStatus(users)
                .flatMap(r -> subjectService.listUsers(depid)
                        .map(users1 -> ResultVO.success(Map.of("users", users1))));

    }

    @GetMapping("timetables/weeks/{week}/dayweeks/{dayweek}")
    public Mono<ResultVO> getTimetables(
            @PathVariable int week, @PathVariable int dayweek,
            @RequestAttribute(RequestConstant.DEPID) String depid) {
        return subjectService.listTimetable(depid, week, dayweek)
                .map(timetables -> ResultVO.success(Map.of("timetables", timetables)));
    }

    @GetMapping("invidetails/counts")
    public Mono<ResultVO> getCounts(@RequestAttribute(RequestConstant.DEPID) String depid) {
        return subjectService.listInviCount(depid)
                .map(counts -> ResultVO.success(Map.of("counts", counts)));
    }

    @GetMapping("invis/{id}")
    public Mono<ResultVO> getInviDetail(@PathVariable String id) {
        return subjectService.getInvi(id)
                .map(invi -> ResultVO.success(Map.of("invi", invi)));
    }

    @GetMapping("invis/dates/{date}")
    public Mono<ResultVO> getDateInvis(@RequestAttribute(RequestConstant.DEPID) String depid,
                                       @PathVariable LocalDate date) {
        return subjectService.listInvis(depid, date)
                .map(invigilations -> ResultVO.success(Map.of("invis", invigilations)));
    }

    @PostMapping("invidetails")
    public Mono<ResultVO> postInviDetails(@RequestBody AssignUserDTO assignUser) {
        List<Mono<String>> monos = new ArrayList<>();
        // 发送取消监考通知，移除日程
        if (StringUtils.hasLength(assignUser.getCalendarId())){
            Mono<String> noticeCanM = dingtalkService.noticeCancel(assignUser.getOldDingUserIds(), assignUser.getCancelMessage());
            Mono<String> delM = dingtalkService.deleteCalender(assignUser.getCreateUnionId(), assignUser.getCalendarId())
                    .flatMap(r -> subjectService.updateInviCalanderNull(assignUser.getInviId()));
            monos.add(noticeCanM);
            monos.add(delM);
        }

        return Flux.merge(monos).collectList()
                .flatMap(r -> subjectService.addInvidetails(assignUser)
                        .map(re -> ResultVO.success(Map.of())));
    }

    @GetMapping("invidetailusers/{inviid}")
    public Mono<ResultVO> getInviUsers(@PathVariable String inviid) {
        return subjectService.listInviDetailUsers(inviid)
                .map(users -> ResultVO.success(Map.of("users", users)));
    }

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
                                        .thenReturn(ResultVO.success(Map.of("code", code)))
                                )
                );
    }
}
