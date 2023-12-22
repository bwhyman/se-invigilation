package com.se.invigilation.service;

import com.aliyun.dingtalkcalendar_1_0.Client;
import com.aliyun.dingtalkcalendar_1_0.models.CreateEventHeaders;
import com.aliyun.dingtalkcalendar_1_0.models.CreateEventRequest;
import com.aliyun.dingtalkcalendar_1_0.models.CreateEventResponse;
import com.aliyun.dingtalkcalendar_1_0.models.DeleteEventResponse;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiMessageCorpconversationAsyncsendV2Request;
import com.dingtalk.api.request.OapiV2UserGetRequest;
import com.dingtalk.api.request.OapiV2UserGetbymobileRequest;
import com.dingtalk.api.request.OapiV2UserListRequest;
import com.dingtalk.api.response.OapiMessageCorpconversationAsyncsendV2Response;
import com.dingtalk.api.response.OapiV2UserGetResponse;
import com.dingtalk.api.response.OapiV2UserGetbymobileResponse;
import com.dingtalk.api.response.OapiV2UserListResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.se.invigilation.component.DingtalkComponent;
import com.se.invigilation.dox.User;
import com.se.invigilation.dto.DingUserListDTO;
import com.se.invigilation.exception.XException;
import com.se.invigilation.repository.InvigilationRepository;
import com.se.invigilation.repository.UserRepository;
import com.taobao.api.ApiException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class DingtalkService {
    private final DingtalkComponent dingtalkComponent;
    private final ObjectMapper objectMapper;
    private final InvigilationRepository invigilationRepository;
    private final UserRepository userRepository;
    @Value("${dingtalk.agentid}")
    private String agentId;
    private Client eventClient;
    private DingTalkClient noticeClient;
    String timeFormat = "%sT%s:00+08:00";

    @PostConstruct
    public void createClient() throws Exception {
        Config config = new Config();
        config.protocol = "https";
        config.regionId = "central";
        eventClient = new Client(config);
        noticeClient = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/message/corpconversation/asyncsend_v2");
    }

    private String timeStamp(String text) {
        String token = String.valueOf(LocalTime.now().getNano()).substring(0, 6);
        return text + "\n\n" + "TOKEN: " + token;
    }

    /**
     * 向专业，发送专业监考分配通知
     *
     * @param userIds "manager4660, 1234, 4545"
     * @return respbody
     */
    public Mono<String> sendNotice(String userIds, String message) {
        OapiMessageCorpconversationAsyncsendV2Request.Msg msg = new OapiMessageCorpconversationAsyncsendV2Request.Msg();
        msg.setMsgtype("text");
        OapiMessageCorpconversationAsyncsendV2Request.Text text = new OapiMessageCorpconversationAsyncsendV2Request.Text();
        text.setContent(timeStamp(message));
        msg.setText(text);

        OapiMessageCorpconversationAsyncsendV2Request req = new OapiMessageCorpconversationAsyncsendV2Request();
        req.setAgentId(Long.valueOf(agentId));
        req.setUseridList(userIds);
        req.setMsg(msg);
        try {
            OapiMessageCorpconversationAsyncsendV2Response rsp = noticeClient.execute(req, dingtalkComponent.getDingtalkToken());
            log.debug("rsp.getBody(): {}", rsp.getBody());
            return Mono.just(rsp.getBody());
        } catch (ApiException e) {
            return Mono.error(XException.builder().codeN(400).message(e.getMessage()).build());
        }
    }

    public Mono<String> addCalander(String createUnionId,
                                    String date,
                                    String stime,
                                    String etime,
                                    List<String> unionIds,
                                    String message,
                                    int remindMinutes) {
        CreateEventHeaders createEventHeaders = new CreateEventHeaders();
        createEventHeaders.xAcsDingtalkAccessToken = dingtalkComponent.getDingtalkToken();
        List<CreateEventRequest.CreateEventRequestAttendees> list = new ArrayList<>();
        for (String id : unionIds) {
            CreateEventRequest.CreateEventRequestAttendees att = new CreateEventRequest.CreateEventRequestAttendees();
            att.setId(id);
            list.add(att);
        }
        CreateEventRequest.CreateEventRequestReminders reminders0 = new CreateEventRequest.CreateEventRequestReminders()
                .setMethod("dingtalk")
                .setMinutes(remindMinutes);
        CreateEventRequest.CreateEventRequestReminders reminders1 = new CreateEventRequest.CreateEventRequestReminders()
                .setMethod("dingtalk")
                .setMinutes(120);
        CreateEventRequest.CreateEventRequestStart start = new CreateEventRequest.CreateEventRequestStart()
                .setDateTime(timeFormat.formatted(date, stime))
                .setTimeZone("Asia/Shanghai");
        CreateEventRequest.CreateEventRequestEnd end = new CreateEventRequest.CreateEventRequestEnd()
                .setDateTime(timeFormat.formatted(date, etime))
                .setTimeZone("Asia/Shanghai");
        CreateEventRequest createEventRequest = new CreateEventRequest()
                .setSummary("监考信息")
                .setStart(start)
                .setEnd(end)
                .setReminders(List.of(reminders0, reminders1))
                .setDescription(timeStamp(message))
                .setExtra(Map.of("noPushNotification", "false"))
                .setAttendees(list);

        try {
            CreateEventResponse resp = eventClient.createEventWithOptions(createUnionId,
                    "primary",
                    createEventRequest,
                    createEventHeaders,
                    new RuntimeOptions());
            return Mono.just(resp.getBody().getId());
        } catch (Exception _err) {
            TeaException err = new TeaException(_err.getMessage(), _err);
            log.debug("{}/{}", err.getCode(), err.getMessage());
            return Mono.error(XException.builder().codeN(400).message(err.getMessage()).build());
        }
    }

    public Mono<String> deleteCalender(String unionid, String eventid) {
        com.aliyun.dingtalkcalendar_1_0.models.DeleteEventHeaders deleteEventHeaders = new com.aliyun.dingtalkcalendar_1_0.models.DeleteEventHeaders();
        deleteEventHeaders.xAcsDingtalkAccessToken = dingtalkComponent.getDingtalkToken();
        try {
            DeleteEventResponse deleteEventResponse = eventClient.deleteEventWithOptions(
                    unionid, "primary", eventid, deleteEventHeaders, new RuntimeOptions());
            log.debug("getStatusCode(): {}", deleteEventResponse.getStatusCode());
            return Mono.just("deleteEventResponse.getStatusCode()");
        } catch (Exception _err) {
            log.debug("deleteCalender Exception{}", _err.getMessage());
            TeaException err = new TeaException(_err.getMessage(), _err);
            log.debug("{}/{}", err.getCode(), err.getMessage());
            return Mono.error(XException.builder().codeN(400).message(err.getMessage()).build());
        }
    }

    public Mono<Integer> cancel(String inviid) {
        return invigilationRepository.findById(inviid).flatMap((invi) -> {
            if (!StringUtils.hasLength(invi.getCalendarId())) {
                return Mono.just(0);
            } else {
                try {
                    String message = "监考取消：%s; %s";
                    Map<String, String> time = objectMapper.readValue(invi.getTime(), new TypeReference<>() {
                    });
                    Map<String, String> course = objectMapper.readValue(invi.getCourse(), new TypeReference<>() {
                    });
                    String cancelMessage = message.formatted(invi.getDate() + " " + time.get("starttime"), course.get("courseName"));
                    return userRepository.findByInviId(inviid).collectList()
                            .map(users -> users.stream().map(User::getDingUserId).toList())
                            .map((userIds) -> String.join(",", userIds))
                            .flatMap((ids) -> sendNotice(ids, cancelMessage))
                            .flatMap((r) -> deleteCalender(invi.getCreateUnionId(), invi.getCalendarId())).
                            thenReturn(1);
                } catch (JsonProcessingException var12) {
                    return Mono.error(XException.builder().codeN(400).message(var12.getMessage()).build());
                }
            }
        });
    }

    // 导出全部用户钉钉信息
    public Mono<List<DingUserListDTO.DingUser>> listDingUsers(long deptId) {
        boolean hasNext = true;
        long nextCursor = 0;
        List<DingUserListDTO.DingUser> users = new ArrayList<>();
        try {
            while (hasNext) {
                log.debug("DingUserListDTO.Result result");
                DingUserListDTO.Result result = listUsers(deptId, nextCursor);
                users.addAll(result.getList());
                hasNext = result.getHas_more();
                if (hasNext) {
                    nextCursor = result.getNext_cursor();
                }
            }
            return Mono.just(users);

        } catch (Exception e) {
            return Mono.error(XException.builder().codeN(400).message(e.getMessage()).build());
        }
    }

    private final DingTalkClient listUsersclient = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/v2/user/list");

    private DingUserListDTO.Result listUsers(long deptId, long nextCursor) throws Exception {
        OapiV2UserListRequest req = new OapiV2UserListRequest();
        req.setDeptId(deptId);
        req.setCursor(nextCursor);
        req.setSize(40L);
        OapiV2UserListResponse rsp = listUsersclient.execute(req, dingtalkComponent.getDingtalkToken());
        log.debug(rsp.getBody());
        DingUserListDTO dingUserListDTO = objectMapper.readValue(rsp.getBody(), DingUserListDTO.class);
        return dingUserListDTO.getResult();
    }

    // 基于钉钉手机号，查询用户userid/unionid
    public Mono<DingUserListDTO.DingUser> getDingUser(String mobile) {
        return getUserid(mobile)
                .flatMap(this::getUnionid);
    }

    private Mono<String> getUserid(String mobile) {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/v2/user/getbymobile");
        OapiV2UserGetbymobileRequest req = new OapiV2UserGetbymobileRequest();
        req.setMobile(mobile);
        try {
            OapiV2UserGetbymobileResponse rsp = client.execute(req, dingtalkComponent.getDingtalkToken());
            if(!rsp.getErrmsg().equals("ok")) {
                return Mono.error(XException.builder().codeN(400).message(rsp.getErrmsg()).build());
            }
            return Mono.just(rsp.getResult().getUserid());
        } catch (ApiException e) {
            return Mono.error(XException.builder().codeN(400).message(e.getMessage()).build());
        }
    }

    private Mono<DingUserListDTO.DingUser> getUnionid(String userid) {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/v2/user/get");
        OapiV2UserGetRequest req = new OapiV2UserGetRequest();
        req.setUserid(userid);
        try {
            OapiV2UserGetResponse rsp = client.execute(req, dingtalkComponent.getDingtalkToken());
            return Mono.just(DingUserListDTO.DingUser.builder()
                    .userid(userid)
                    .unionid(rsp.getResult().getUnionid())
                    .name(rsp.getResult().getName())
                    .build());
        } catch (ApiException e) {
            return Mono.error(XException.builder().codeN(400).message(e.getMessage()).build());
        }
    }
}
