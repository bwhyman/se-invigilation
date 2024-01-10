package com.se.invigilation.controller;

import com.se.invigilation.dox.Department;
import com.se.invigilation.dox.Invigilation;
import com.se.invigilation.dox.Timetable;
import com.se.invigilation.dox.User;
import com.se.invigilation.dto.AssignUserDTO;
import com.se.invigilation.dto.NoticeDTO;
import com.se.invigilation.dto.NoticeRemarkDTO;
import com.se.invigilation.service.CollegeService;
import com.se.invigilation.service.DingtalkService;
import com.se.invigilation.service.SubjectService;
import com.se.invigilation.vo.RequestConstant;
import com.se.invigilation.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final SubjectService subjectService;
    private final PasswordEncoder passwordEncoder;

    // 批量导入监考
    @PostMapping("invigilations")
    public Mono<ResultVO> postInvigilations(@RequestBody List<Invigilation> invigilations) {
        return collegeService.addInvigilations(invigilations)
                .thenReturn(ResultVO.success(Map.of()));
    }

    // 获取开放状态部门
    @GetMapping("departments/opened")
    public Mono<ResultVO> getOpenedDepartments(@RequestAttribute(RequestConstant.COLLID) String collid) {
        return collegeService.listDepartments(collid, Department.OPEN).map((deps) ->
                ResultVO.success(Map.of("departments", deps)));
    }

    // 获取全部导入状态监考
    @GetMapping("invilations/imported")
    public Mono<ResultVO> getImporteds(@RequestAttribute(RequestConstant.COLLID) String collid) {
        return collegeService.listImporteds(collid).map((invis) ->
                ResultVO.success(Map.of("invis", invis)));
    }

    // 按部门，页数，获取下发状态监考
    @GetMapping("invilations/dispatched/{depid}/{page}")
    public Mono<ResultVO> getDispatcheds(@PathVariable String depid, @PathVariable int page) {
        Pageable pageable = PageRequest.of(page - 1, RequestConstant.pageSize);
        return collegeService.listDispatchedInvis(depid, pageable).map((invis) ->
                ResultVO.success(Map.of("invis", invis)));
    }

    // 获取指定部门下发状态监考数量
    @GetMapping("invigilations/dispatched/{depid}/total")
    public Mono<ResultVO> getDispatchedTotals(@PathVariable String depid) {
        return collegeService.getdispatchedTotal(depid).map((total) ->
                ResultVO.success(Map.of("total", total)));
    }

    // 更新监考状态为下发状态
    @PatchMapping("invigilations/dispatch")
    public Mono<ResultVO> patchInvigilations(@RequestBody List<Invigilation> invigilations) {
        return collegeService.updateDispatcher(invigilations).map((r) ->
                ResultVO.success(Map.of()));
    }

    // 获取全学院教师
    @GetMapping("users")
    public Mono<ResultVO> getUsers(@RequestAttribute(RequestConstant.COLLID) String collid) {
        return collegeService.listUser(collid).map((users) ->
                ResultVO.success(Map.of("users", users)));
    }

    // 先移除学院课表，导入全院教师课表
    @PostMapping("timetables")
    public Mono<ResultVO> postTimetables(@RequestAttribute(RequestConstant.COLLID) String collid,
                                         @RequestBody List<Timetable> timetables) {
        for (Timetable tb : timetables) {
            tb.setCollId(collid);
        }
        return collegeService.removeCollegeTimetables(collid)
                .flatMap(r -> collegeService.addTimetables(timetables))
                .thenReturn(ResultVO.success(Map.of()));
    }

    // 获取指定部门监考分配教师
    @GetMapping("dispatchers/{depid}")
    public Mono<ResultVO> getSubjectDispatchers(@PathVariable String depid) {
        return collegeService.listUsers(depid, User.SUBJECT_ADMIN).map((users) ->
                ResultVO.success(Map.of("users", users)));
    }

    // 向部门监考分配教师，发送分配监考提醒
    @PostMapping({"dispatchnotices"})
    public Mono<ResultVO> postDispatchNotice(@RequestBody NoticeDTO notice) {
        return dingtalkService.sendNotice(notice.getUserIds(), notice.getNoticeMessage()).map((result) ->
                        ResultVO.success(Map.of("dingResp", result)))
                .onErrorResume((e) -> Mono.just(ResultVO.error(400, e.getMessage())));
    }

    // 手动添加一个监考
    @PostMapping("invigilation")
    public Mono<ResultVO> postInvigilation(@RequestBody Invigilation invigilation,
                                           @RequestAttribute(RequestConstant.COLLID) String collid) {
        invigilation.setCollId(collid);
        return collegeService.addInvigilation(invigilation)
                .thenReturn(ResultVO.success(Map.of()));
    }

    // 导入单教师课表
    @PostMapping("timetables/{userid}")
    public Mono<ResultVO> postTimetable(@RequestAttribute(RequestConstant.COLLID) String collid,
                                        @PathVariable String userid,
                                        @RequestBody List<Timetable> timetables) {
        for (Timetable tb : timetables) {
            tb.setCollId(collid);
        }
        return collegeService.addTimetable(userid, timetables)
                .thenReturn(ResultVO.success(Map.of()));
    }

    // 删除监考详细信息，删除监考信息
    @DeleteMapping("invigilations/{inviid}")
    public Mono<ResultVO> deleteInvigilation(@PathVariable String inviid) {
        return collegeService.removeInvigilation(inviid).
                thenReturn(ResultVO.success(Map.of()));
    }

    // 更新监考基本信息
    @PatchMapping("invigilations/edit")
    public Mono<ResultVO> patchInvigilation(@RequestBody Invigilation invigilation) {
        return collegeService.updateInvigilations(invigilation)
                .thenReturn(ResultVO.success(Map.of()));
    }

    // 重置监考至导入状态
    @PutMapping("invigilations/{inviid}/status")
    public Mono<ResultVO> putInvigilationStatus(@PathVariable String inviid) {
        return collegeService.resetInvigilation(inviid)
                .thenReturn(ResultVO.success(Map.of()));
    }

    // 更新部门监考状态
    @PatchMapping("departments/invistatus")
    public Mono<ResultVO> patchDepartmentsInviStatus(@RequestAttribute(RequestConstant.COLLID) String collid,
                                                     @RequestBody List<Department> departments) {
        return collegeService.updateDepartmentInviStatus(departments).flatMap((r) ->
                collegeService.listDepartments(collid).map((departs) ->
                        ResultVO.success(Map.of("departments", departs))));
    }

    // 获取全部部门
    @GetMapping("departments")
    public Mono<ResultVO> getDepartments(@RequestAttribute(RequestConstant.COLLID) String collid) {
        return collegeService.listDepartments(collid).map((deps) ->
                ResultVO.success(Map.of("departments", deps)));
    }

    // 基于教师姓名及专业ID，查找用户
    @GetMapping("departments/{depid}/names/{name}")
    public Mono<ResultVO> getUsersName(@PathVariable String depid, @PathVariable String name) {
        return collegeService.listUsersByName(depid, name)
                .map(users -> ResultVO.success(Map.of("users", users)));
    }

    // 教师自己的主考，学院要自己分配
    @PostMapping("assigns/invis/{inviid}")
    public Mono<ResultVO> postAssigns(@PathVariable String inviid, @RequestBody AssignUserDTO assignUser) {
        return subjectService.updateInviCalanderNull(inviid).flatMap((r) ->
                subjectService.addInvidetails(inviid, assignUser).map((re) ->
                        ResultVO.success(Map.of())));
    }

    // 修改指定账号角色
    @PostMapping("roles")
    public Mono<ResultVO> postRole(@RequestBody User user) {
        return collegeService.updateRole(user.getId(), user.getRole())
                .thenReturn(ResultVO.success(Map.of()));
    }

    // 获取全部监考信息
    @GetMapping("invis/all")
    public Mono<ResultVO> getInvis(@RequestAttribute(RequestConstant.COLLID) String collid) {
        return collegeService.listInvis(collid)
                .map(invis -> ResultVO.success(Map.of("invis", invis)));
    }

    //获取学院教师监考数量
    @GetMapping("invis/counts")
    public Mono<ResultVO> getInvisCounts(@RequestAttribute(RequestConstant.COLLID) String collid) {
        return collegeService.listCollCounts(collid)
                .map(counts -> ResultVO.success(Map.of("counts", counts)));
    }

    @PostMapping("departments/updateuser")
    public Mono<ResultVO> postDepartment(@RequestBody User user) {
        return collegeService.updateUserDepartment(user.getId(), user.getDepartment())
                .thenReturn(ResultVO.success(Map.of()));
    }

    // 重置密码
    @PutMapping("passwords/{account}")
    public Mono<ResultVO> putPassword(@PathVariable String account) {
        return collegeService.updatePassword(account)
                .thenReturn(ResultVO.success(Map.of()));
    }

    // 手动添加包含钉钉2个账号的用户
    @PostMapping("users")
    public Mono<ResultVO> postUser(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getAccount()));
        return collegeService.addUser(user)
                .thenReturn(ResultVO.success(Map.of()));
    }

    // 加载指定日期内全部监考
    @GetMapping("invis/date/{sdate}/{edate}")
    public Mono<ResultVO> getinvisDate(
            @PathVariable String sdate,
            @PathVariable String edate,
            @RequestAttribute(RequestConstant.COLLID) String collid) {
        return collegeService.listInvisByDate(collid, sdate, edate)
                .map(invis -> ResultVO.success(Map.of("invis", invis)));
    }

    // 发送监考备注工作通知
    @PostMapping("invinotices")
    public Mono<ResultVO> postDingIds(@RequestBody NoticeRemarkDTO notice) {
        return collegeService.updateInviRemark(notice.getInviIds(), notice.getRemark())
                .flatMap(c -> dingtalkService.sendNotice(notice.getDingUserIds(), notice.getRemark())
                        .map(result -> ResultVO.success(Map.of("result", result))));
    }

    @GetMapping("invis/{id}")
    public Mono<ResultVO> getInviDetail(@PathVariable String id,
                                        @RequestAttribute(RequestConstant.COLLID) String collid) {
        return collegeService.getInvigilation(collid, id).map((invi) ->
                ResultVO.success(Map.of("invi", invi)));
    }

    // 剪裁监考。将指定监考人数减一，并复制一份独立的新监考。
    @PostMapping("cutinvigilation/{oldInviid}")
    public Mono<ResultVO> postInvigilation(@PathVariable String oldInviid,
                                           @RequestBody Invigilation invi,
                                           @RequestAttribute(RequestConstant.COLLID) String collid) {
        invi.setCollId(collid);
        return collegeService.updateInvigilations(oldInviid, invi)
                .flatMap(r -> collegeService.listImporteds(collid)
                        .map(invis -> ResultVO.success(Map.of("invis", invis))));
    }

    // 基于姓名获取用户
    @GetMapping("users/{name}")
    public Mono<ResultVO> getUser(@PathVariable String name,
                                  @RequestAttribute(RequestConstant.COLLID) String collid) {
        return collegeService.getUser(collid, name)
                .map(users -> ResultVO.success(Map.of("users", users)));
    }

    // 获取专业全部教师。用于学院直接分配的检索
    @GetMapping("department/{depid}/users")
    public Mono<ResultVO> getDepartmentUsers(@PathVariable String depid) {
        return subjectService.listUsers(depid)
                .map(users -> ResultVO.success(Map.of("users", users)));
    }
    @GetMapping("mobiles/{m}")
    public Mono<ResultVO> getDingUser(@PathVariable String m) {
        return dingtalkService.getDingUser(m)
                .map(dingUser -> ResultVO.success(Map.of("dinguser", dingUser)));
    }

    @DeleteMapping("users/{uid}")
    public Mono<ResultVO> deleteUser(@PathVariable String uid) {
        return collegeService.removeUser(uid)
                .thenReturn(ResultVO.success(Map.of()));
    }
}
