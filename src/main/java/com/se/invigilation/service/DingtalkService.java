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
import com.dingtalk.api.response.OapiMessageCorpconversationAsyncsendV2Response;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.se.invigilation.component.DingtalkComponent;
import com.se.invigilation.dox.User;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
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


    /**
     * 向专业，发送专业监考分配通知
     * @param userIds "manager4660, 1234, 4545"
     * @return respbody
     */
    public Mono<String> noticeDispatchers(String userIds) {
        OapiMessageCorpconversationAsyncsendV2Request.Msg msg = new OapiMessageCorpconversationAsyncsendV2Request.Msg();
        msg.setMsgtype("text");
        OapiMessageCorpconversationAsyncsendV2Request.Text text = new OapiMessageCorpconversationAsyncsendV2Request.Text();
        text.setContent(timeStamp("已下发新监考信息，请及时分配。"));
        msg.setText(text);

        OapiMessageCorpconversationAsyncsendV2Request req = new OapiMessageCorpconversationAsyncsendV2Request();
        req.setAgentId(Long.valueOf(agentId));
        req.setUseridList(userIds);
        req.setMsg(msg);
        try {
            OapiMessageCorpconversationAsyncsendV2Response rsp = noticeClient.execute(req, dingtalkComponent.getDingtalkToken());
            log.debug("rsp.getCode(): {}", rsp.getCode());
            log.debug("rsp.getBody(): {}", rsp.getBody());
            return Mono.just(rsp.getBody());
        } catch (ApiException e) {
            return Mono.error(XException.builder().codeN(200).message(e.getMessage()).build());
        }
    }

    public Mono<String> noticeAssigners(String userIds, String message) {
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
            log.debug("rsp.getCode(): {}", rsp.getCode());
            log.debug("rsp.getBody(): {}", rsp.getBody());
            return Mono.just(rsp.getBody());
        } catch (ApiException e) {
            return Mono.error(XException.builder().codeN(200).message(e.getMessage()).build());
        }
    }

    public Mono<String> addCalander(String createUnionId,
                                    String date,
                                    String stime,
                                    String etime,
                                    List<String> unionIds,
                                    String message) {
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
                .setMinutes(1440);
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
            log.debug("resp.getBody(): {}", resp.getBody());
            log.debug("resp.getBody().getId(): {}", resp.getBody().getId());
            return Mono.just(resp.getBody().getId());
        } catch (Exception _err) {
            TeaException err = new TeaException(_err.getMessage(), _err);
            log.debug("{}/{}", err.getCode(), err.getMessage());
            return Mono.error(XException.builder().codeN(200).message(err.getMessage()).build());
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
            return Mono.error(XException.builder().codeN(200).message(err.getMessage()).build());
        }
    }
    public Mono<String> noticeCancel(String userIds, String message) {
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
            log.debug("rsp.getCode(): {}", rsp.getCode());
            log.debug("rsp.getBody(): {}", rsp.getBody());
            return Mono.just(rsp.getBody());
        } catch (ApiException e) {
            return Mono.error(XException.builder().codeN(200).message(e.getMessage()).build());
        }
    }

    public Mono<Integer> cancel(String inviid) {
        return this.invigilationRepository.findById(inviid).flatMap((invi) -> {
            if (!StringUtils.hasLength(invi.getCalendarId())) {
                return Mono.just(0);
            } else {
                List<Mono<String>> dingUserIds = new ArrayList<>();
                try {
                    String message = "监考取消：%s; %s";
                    List<Map<String, String>> users = objectMapper.readValue(invi.getExecutor(), new TypeReference<>() {});
                    Map<String, String> time = objectMapper.readValue(invi.getTime(), new TypeReference<>() {});
                    Map < String, String > course = objectMapper.readValue(invi.getCourse(), new TypeReference<>() {});
                    String cancelMessage = message.formatted(invi.getDate() + " " + time.get("starttime"), course.get("courseName"));
                    for (Map<String, String> user : users) {
                        Mono<String> byId = this.userRepository.findById(user.get("userId")).map(User::getDingUserId);
                        dingUserIds.add(byId);
                    }

                    return Flux.merge(dingUserIds).collectList().
                            map((userIds) -> String.join(",", userIds))
                            .flatMap((ids) -> noticeCancel(ids, cancelMessage))
                            .flatMap((r) -> deleteCalender(invi.getCreateUnionId(), invi.getCalendarId())).
                            thenReturn(1);
                } catch (JsonProcessingException var12) {
                    return Mono.error(XException.builder().codeN(200).message(var12.getMessage()).build());
                }
            }
        });
    }

    private String timeStamp(String text) {
        return text + "\n" + "TOKEN: " + LocalDateTime.now().getNano();
    }

}
