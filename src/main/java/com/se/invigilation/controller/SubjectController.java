package com.se.invigilation.controller;

import com.se.invigilation.dox.ExcludeRule;
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
                .map(ResultVO::success);
    }

    // 获取指定状态，指定页，监考
    @GetMapping("invis/dispatcheds")
    public Mono<ResultVO> getDispatchedsInvis(@RequestAttribute(RequestConstant.DEPID) String depid) {
        return subjectService.listDispatchedInvigilations(depid)
                .map(ResultVO::success);
    }

    // 获取指定状态，指定页，监考
    @GetMapping("invis/status/{status}/{page}")
    public Mono<ResultVO> getInvis(@PathVariable int status,
                                   @PathVariable int page,
                                   @RequestAttribute(RequestConstant.DEPID) String depid) {
        Pageable pageable = PageRequest.of(page - 1, RequestConstant.pageSize);
        return subjectService.listDispatchedInvigilations(depid, status, pageable)
                .map(ResultVO::success);
    }

    // 获取指定状态监考数量
    @GetMapping("/invis/status/{status}/total")
    public Mono<ResultVO> getInvisStatusTotal(@PathVariable int status,
                                              @RequestAttribute(RequestConstant.DEPID) String depid) {
        return subjectService.getInvisTotal(depid, status)
                .map(ResultVO::success);
    }

    // 更新教师监考状态
    @PostMapping("invistatus")
    public Mono<ResultVO> postInviStatus(@RequestBody List<User> users,
                                         @RequestAttribute(RequestConstant.DEPID) String depid) {
        return subjectService.updateUserInviStatus(users, depid)
                .then(subjectService.listUsers(depid))
                .map(ResultVO::success);
    }

    @GetMapping("invis/{id}")
    public Mono<ResultVO> getInviDetail(@PathVariable String id,
                                        @RequestAttribute(RequestConstant.DEPID) String depid) {
        return subjectService.getInvigilation(depid, id)
                .map(ResultVO::success);
    }

    // 获取开放状态教师，指定周/星期的全部课表
    @GetMapping("timetables/weeks/{week}/dayweeks/{dayweek}")
    public Mono<ResultVO> getTimetables(@PathVariable int week,
                                        @PathVariable int dayweek,
                                        @RequestAttribute(RequestConstant.DEPID) String depid) {
        return subjectService.listTimetable(depid, week, dayweek)
                .map(ResultVO::success);
    }

    // 获取部门教师监考数量
    @GetMapping("invidetails/counts")
    public Mono<ResultVO> getCounts(@RequestAttribute(RequestConstant.DEPID) String depid) {
        return subjectService.listDepUserCounts(depid)
                .map(ResultVO::success);
    }

    // 获取指定日期全部监考
    @GetMapping("invis/dates/{date}")
    public Mono<ResultVO> getDateInvis(@PathVariable LocalDate date,
                                       @RequestAttribute(RequestConstant.DEPID) String depid) {
        return subjectService.listDispatchedInvigilations(depid, date)
                .map(ResultVO::success);
    }

    // 删除原监考详细信息；创建新监考详细信息
    @PostMapping("invidetails/{inviid}")
    public Mono<ResultVO> postInviDetails(@PathVariable String inviid,
                                          @RequestAttribute(RequestConstant.DEPID) String depid,
                                          @RequestBody AssignUserDTO assignUser) {
        return subjectService.updateInviCalanderNull(inviid, depid)
                .then(subjectService.addInvidetails(inviid, assignUser))
                .thenReturn(ResultVO.ok());
    }

    // 获取指定监考教师信息，及钉钉信息
    @GetMapping("invidetailusers/{inviid}")
    public Mono<ResultVO> getInviUsers(@PathVariable String inviid) {
        return subjectService.listInviDetailUsers(inviid)
                .map(ResultVO::success);
    }

    // 发送钉钉监考通知，监考日程
    @PostMapping("assignnotices")
    public Mono<ResultVO> postAssignNotices(@RequestBody NoticeDTO notice) {
        return dingtalkService.sendNotice(notice.getUserIds(), notice.getNoticeMessage())
                .then(dingtalkService.addCalander(notice.getCreateUnionId(),
                        notice.getDate(),
                        notice.getStime(),
                        notice.getEtime(),
                        notice.getUnionIds(),
                        notice.getNoticeMessage(),
                        notice.getRemindMinutes()))
                .flatMap(eventId -> subjectService.updateInviCalanderId(
                        notice.getInviId(),
                        eventId, notice.getCreateUnionId(),
                        notice.getNoticeUserIds()))
                .map(ResultVO::success);
    }

    @GetMapping("comments")
    public Mono<ResultVO> getComment(@RequestAttribute(RequestConstant.DEPID) String depid) {
        return subjectService.getDepartmentComment(depid)
                .map(ResultVO::success);
    }

    // 专业添加如某教师周末不分配的监考备忘录
    @PostMapping("comments")
    public Mono<ResultVO> postComments(@RequestBody Map<String, String> comm,
                                       @RequestAttribute(RequestConstant.DEPID) String depid) {

        return subjectService.updateComment(depid, comm.get("comment"))
                .thenReturn(ResultVO.ok());
    }

    // 不排监考的排除规则
    @PostMapping("excluderules")
    public Mono<ResultVO> postExculdeRule(@RequestBody ExcludeRule rule,
                                          @RequestAttribute(RequestConstant.DEPID) String depid) {
        rule.setDepId(depid);
        return subjectService.addExculdeRule(rule)
                .then(subjectService.listExcludeRules(depid))
                .map(ResultVO::success);
    }

    @GetMapping("excluderules")
    public Mono<ResultVO> getExculdeRules(@RequestAttribute(RequestConstant.DEPID) String depid) {
        return subjectService.listExcludeRules(depid)
                .map(ResultVO::success);
    }

    @DeleteMapping("excluderules/{exid}")
    public Mono<ResultVO> delExculdeRules(@PathVariable String exid,
                                          @RequestAttribute(RequestConstant.DEPID) String depid) {
        return subjectService.removeExculdeRule(exid)
                .then(subjectService.listExcludeRules(depid))
                .map(ResultVO::success);
    }

    @GetMapping("invis/all")
    public Mono<ResultVO> getInvisALl(@RequestAttribute(RequestConstant.DEPID) String depid) {
        return subjectService.listInvisByDepId(depid)
                .map(ResultVO::success);
    }
}
