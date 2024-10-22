package com.se.invigilation.controller;

import com.se.invigilation.dox.Department;
import com.se.invigilation.dox.Invigilation;
import com.se.invigilation.dox.Timetable;
import com.se.invigilation.dox.User;
import com.se.invigilation.dto.AssignUserDTO;
import com.se.invigilation.dto.DepartmentDTO;
import com.se.invigilation.dto.NoticeDTO;
import com.se.invigilation.dto.NoticeRemarkDTO;
import com.se.invigilation.exception.Code;
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
    public Mono<ResultVO> postInvigilations(@RequestBody List<Invigilation> invigilations,
                                            @RequestAttribute(RequestConstant.COLLID) String collid) {
        for (Invigilation invigilation : invigilations) {
            invigilation.setCollId(collid);
        }
        return collegeService.addInvigilations(invigilations)
                .map(ResultVO::success);
    }

    // 获取开放状态部门
    @GetMapping("departments/opened")
    public Mono<ResultVO> getOpenedDepartments(@RequestAttribute(RequestConstant.COLLID) String collid) {
        return collegeService.listDepartments(collid, Department.OPEN)
                .map(ResultVO::success);
    }

    // 获取全部导入状态监考
    @GetMapping("invilations/imported")
    public Mono<ResultVO> getImporteds(@RequestAttribute(RequestConstant.COLLID) String collid) {
        return collegeService.listImporteds(collid)
                .map(ResultVO::success);
    }

    // 按部门，页数，获取下发状态监考
    @GetMapping("invilations/dispatched/{depid}/{page}")
    public Mono<ResultVO> getDispatcheds(@PathVariable String depid,
                                         @PathVariable int page,
                                         @RequestAttribute(RequestConstant.COLLID) String collid) {
        Pageable pageable = PageRequest.of(page - 1, RequestConstant.pageSize);
        return collegeService.listDispatchedInvis(depid, collid, pageable)
                .map(ResultVO::success);
    }

    // 获取指定部门下发状态监考数量
    @GetMapping("invigilations/dispatched/{depid}/total")
    public Mono<ResultVO> getDispatchedTotals(@PathVariable String depid,
                                              @RequestAttribute(RequestConstant.COLLID) String collid) {
        return collegeService.getdispatchedTotal(depid, collid)
                .map(ResultVO::success);
    }

    // 更新监考状态为下发状态
    @PatchMapping("invigilations/dispatch")
    public Mono<ResultVO> patchInvigilations(@RequestBody List<Invigilation> invigilations,
                                             @RequestAttribute(RequestConstant.COLLID) String collid) {
        return collegeService.updateDispatcher(invigilations, collid)
                .then(collegeService.listImporteds(collid))
                .map(ResultVO::success);
    }

    // 获取全学院教师
    @GetMapping("users")
    public Mono<ResultVO> getUsers(@RequestAttribute(RequestConstant.COLLID) String collid) {
        return collegeService.listUser(collid)
                .map(ResultVO::success);
    }

    // 先移除学院课表，导入全院教师课表
    @PostMapping("timetables")
    public Mono<ResultVO> postTimetables(@RequestBody List<Timetable> timetables,
                                         @RequestAttribute(RequestConstant.COLLID) String collid) {
        return collegeService.addTimetables(timetables, collid)
                .thenReturn(ResultVO.ok());
    }

    // 获取指定部门监考分配教师
    @GetMapping("dispatchers/{depid}")
    public Mono<ResultVO> getSubjectDispatchers(@PathVariable String depid,
                                                @RequestAttribute(RequestConstant.COLLID) String collid) {
        return collegeService.listUsers(depid, collid, User.SUBJECT_ADMIN)
                .map(ResultVO::success);
    }

    // 向部门监考分配教师，发送分配监考提醒
    @PostMapping("dispatchnotices")
    public Mono<ResultVO> postDispatchNotice(@RequestBody NoticeDTO notice) {
        return dingtalkService.sendNotice(notice.getUserIds(), notice.getNoticeMessage())
                .map(ResultVO::success);
    }

    // 导入单教师课表
    @PostMapping("timetables/{userid}")
    public Mono<ResultVO> postTimetable(@PathVariable String userid,
                                        @RequestBody List<Timetable> timetables,
                                        @RequestAttribute(RequestConstant.COLLID) String collid) {
        for (Timetable tb : timetables) {
            tb.setCollId(collid);
        }
        return collegeService.addTimetable(userid, timetables)
                .thenReturn(ResultVO.ok());
    }

    // 删除监考详细信息，删除监考信息
    @DeleteMapping("invigilations/{inviid}")
    public Mono<ResultVO> deleteInvigilation(@PathVariable String inviid,
                                             @RequestAttribute(RequestConstant.COLLID) String collid) {
        return collegeService.removeInvigilation(inviid, collid)
                .thenReturn(ResultVO.ok());
    }

    // 更新监考基本信息
    @PatchMapping("invigilations/edit")
    public Mono<ResultVO> patchInvigilation(@RequestBody Invigilation invigilation) {
        return collegeService.updateInvigilations(invigilation)
                .map(ResultVO::success);
    }

    // 重置监考至导入状态
    @PutMapping("invigilations/{inviid}/status")
    public Mono<ResultVO> putInvigilationStatus(@PathVariable String inviid) {
        return collegeService.resetInvigilation(inviid)
                .thenReturn(ResultVO.ok());
    }

    // 更新部门监考状态
    @PatchMapping("departments/invistatus")
    public Mono<ResultVO> patchDepartmentsInviStatus(@RequestAttribute(RequestConstant.COLLID) String collid,
                                                     @RequestBody List<Department> departments) {
        return collegeService.updateDepartmentInviStatus(departments)
                .then(collegeService.listDepartments(collid))
                .map(ResultVO::success);
    }

    // 获取全部部门
    @GetMapping("departments")
    public Mono<ResultVO> getDepartments(@RequestAttribute(RequestConstant.COLLID) String collid) {
        return collegeService.listDepartments(collid)
                .map(ResultVO::success);
    }

    // 学院分配
    @PostMapping("assigns/invis/{inviid}")
    public Mono<ResultVO> postAssigns(@PathVariable String inviid,
                                      @RequestAttribute(RequestConstant.COLLID) String collid,
                                      @RequestBody AssignUserDTO assignUser) {
        return collegeService.assignInvilaton(collid, inviid, assignUser)
                .map(ResultVO::success);
    }

    // 获取全部监考信息
    @GetMapping("invis/all")
    public Mono<ResultVO> getInvis(@RequestAttribute(RequestConstant.COLLID) String collid) {
        return collegeService.listInvis(collid)
                .map(ResultVO::success);
    }

    //获取学院教师监考数量
    @GetMapping("invis/counts")
    public Mono<ResultVO> getInvisCounts(@RequestAttribute(RequestConstant.COLLID) String collid) {
        return collegeService.listCollCounts(collid)
                .map(ResultVO::success);
    }

    // 重置密码
    @PutMapping("passwords/{account}")
    public Mono<ResultVO> putPassword(@PathVariable String account,
                                      @RequestAttribute(RequestConstant.COLLID) String collid) {
        return collegeService.updatePassword(account, collid)
                .thenReturn(ResultVO.ok());
    }

    // 手动添加包含钉钉2个账号的用户
    @PostMapping("users")
    public Mono<ResultVO> postUser(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getAccount()));
        return collegeService.addUser(user)
                .thenReturn(ResultVO.ok());
    }

    // 发送监考备注工作通知
    @PostMapping("invinotices")
    public Mono<ResultVO> postDingIds(@RequestBody NoticeRemarkDTO notice) {
        return collegeService.updateInviRemark(notice.getInviIds(), notice.getRemark())
                .then(dingtalkService.sendNotice(notice.getDingUserIds(), notice.getRemark()))
                .map(ResultVO::success);
    }

    @GetMapping("invis/{id}")
    public Mono<ResultVO> getInviDetail(@PathVariable String id,
                                        @RequestAttribute(RequestConstant.COLLID) String collid) {
        return collegeService.getInvigilation(collid, id)
                .map(ResultVO::success);
    }

    // 剪裁监考。将指定监考人数减一，并复制一份独立的新监考。
    @PostMapping("cutinvigilation/{oldInviid}")
    public Mono<ResultVO> postInvigilation(@PathVariable String oldInviid,
                                           @RequestBody Invigilation invi,
                                           @RequestAttribute(RequestConstant.COLLID) String collid) {
        invi.setCollId(collid);
        return collegeService.updateInvigilations(oldInviid, invi)
                .then(collegeService.listImporteds(collid))
                .map(ResultVO::success);
    }

    // 获取专业全部教师。用于学院直接分配的检索
    @GetMapping("department/{depid}/users")
    public Mono<ResultVO> getDepartmentUsers(@PathVariable String depid) {
        return subjectService.listUsers(depid)
                .map(ResultVO::success);
    }

    // 基于手机号，获取钉钉用户信息
    @GetMapping("mobiles/{m}")
    public Mono<ResultVO> getDingUser(@PathVariable String m) {
        return dingtalkService.getDingUser(m)
                .map(ResultVO::success);
    }

    @DeleteMapping("users/{uid}")
    public Mono<ResultVO> deleteUser(@PathVariable String uid,
                                     @RequestAttribute(RequestConstant.COLLID) String collid) {
        return collegeService.removeUser(uid, collid)
                .thenReturn(ResultVO.ok());
    }

    // 基于学院ID移除监考详细分配信息/监考信息/课表，学期前清空数据
    @DeleteMapping("colleges/datareset")
    public Mono<ResultVO> deleteInvis(@RequestAttribute(RequestConstant.COLLID) String collid) {
        return collegeService.removeCollegeData(collid)
                .thenReturn(ResultVO.ok());
    }

    // 移除部门
    @DeleteMapping("departments/{depid}")
    public Mono<ResultVO> deleteDepartment(@PathVariable String depid,
                                           @RequestAttribute(RequestConstant.COLLID) String collid) {
        return subjectService.listUsers(depid)
                .mapNotNull(users -> {
                    if (!users.isEmpty()) {
                        return ResultVO.error(Code.ERROR, "禁止移除用户非空部门");
                    }
                    return null;
                })
                .switchIfEmpty(collegeService.removeDepartment(depid, collid)
                        .then(collegeService.listDepartments(collid))
                        .map(ResultVO::success));

    }

    @PatchMapping("departments/{depid}")
    public Mono<ResultVO> patchDepartment(@PathVariable String depid,
                                          @RequestBody DepartmentDTO depart,
                                          @RequestAttribute(RequestConstant.COLLID) String collid) {

        return collegeService.updateDetparmentName(depid, collid, depart.getDepartmentName())
                .then(collegeService.listDepartments(collid))
                .map(ResultVO::success);
    }

    @PostMapping("departments")
    public Mono<ResultVO> postDepartments(@RequestBody Department department,
                                          @RequestAttribute(RequestConstant.COLLID) String collid) {
        return collegeService.addDepartment(department)
                .then(collegeService.listDepartments(collid))
                .map(ResultVO::success);
    }

    //
    @PatchMapping("users/{uid}")
    public Mono<ResultVO> patchUsers(
            @PathVariable String uid,
            @RequestBody User user,
            @RequestAttribute(RequestConstant.COLLID) String collid) {
        return collegeService.updateUser(uid, user, collid)
                .thenReturn(ResultVO.ok());
    }
}
