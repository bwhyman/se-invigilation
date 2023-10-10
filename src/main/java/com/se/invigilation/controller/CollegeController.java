package com.se.invigilation.controller;

import com.se.invigilation.dox.Department;
import com.se.invigilation.dox.Invigilation;
import com.se.invigilation.dox.Timetable;
import com.se.invigilation.dox.User;
import com.se.invigilation.dto.AssignUserDTO;
import com.se.invigilation.service.CollegeService;
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
    // 导入全院教师课表
    @PostMapping("timetables")
    public Mono<ResultVO> postTimetables(@RequestBody List<Timetable> timetables) {
        return collegeService.addTimetables(timetables)
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
    public Mono<ResultVO> postDispatchNotice(@RequestBody Map<String, String> map) {
        return dingtalkService.noticeDispatchers(map.get("userIds"), map.get("message")).map((result) ->
                        ResultVO.success(Map.of("dingResp", result)))
                .onErrorResume((e) -> Mono.just(ResultVO.error(400, e.getMessage())));
    }

    // 手动添加一个监考
    @PostMapping("invigilation")
    public Mono<ResultVO> postInvigilation(@RequestBody Invigilation invigilation) {
        return collegeService.addInvigilation(invigilation)
                .thenReturn(ResultVO.success(Map.of()));
    }

    // 导入单教师课表
    @PostMapping("timetables/{userid}")
    public Mono<ResultVO> postTimetables(@PathVariable String userid, @RequestBody List<Timetable> timetables) {
        return collegeService.addTimetable(userid, timetables)
                .thenReturn(ResultVO.success(Map.of()));
    }

    // 发送钉钉取消通知；删除监考详细信息，删除监考信息
    @DeleteMapping("invigilations/{inviid}")
    public Mono<ResultVO> deleteInvigilation(@PathVariable String inviid) {
        return dingtalkService.cancel(inviid).flatMap((r) ->
                        collegeService.removeInvigilation(inviid)).
                thenReturn(ResultVO.success(Map.of()));
    }

    // 更新监考基本信息
    @PatchMapping("invigilations/edit")
    public Mono<ResultVO> patchInvigilation(@RequestBody Invigilation invigilation) {
        return collegeService.updateInvigilation(invigilation)
                .thenReturn(ResultVO.success(Map.of()));
    }

    // 发送取消监考通知；重置监考至导入状态
    @PutMapping("invigilations/{inviid}/status")
    public Mono<ResultVO> putInvigilationStatus(@PathVariable String inviid) {
        return dingtalkService.cancel(inviid).flatMap((r) ->
                        collegeService.resetInvigilation(inviid))
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
        return dingtalkService.cancel(inviid).flatMap((r) ->
                subjectService.updateInviCalanderNull(inviid)).flatMap((r) ->
                subjectService.addInvidetails(inviid, assignUser.getDepId(), assignUser).map((re) ->
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
}
