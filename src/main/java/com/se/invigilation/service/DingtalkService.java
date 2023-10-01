package com.se.invigilation.service;

import com.aliyun.dingtalkcalendar_1_0.Client;
import com.aliyun.dingtalkcalendar_1_0.models.CreateEventHeaders;
import com.aliyun.dingtalkcalendar_1_0.models.CreateEventRequest;
import com.aliyun.dingtalkcalendar_1_0.models.CreateEventResponse;
import com.aliyun.dingtalkcalendar_1_0.models.DeleteEventResponse;
import com.aliyun.tea.TeaException;
import com.aliyun.teautil.models.RuntimeOptions;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiMessageCorpconversationAsyncsendV2Request;
import com.dingtalk.api.response.OapiMessageCorpconversationAsyncsendV2Response;
import com.se.invigilation.component.DingtalkComponent;
import com.se.invigilation.exception.XException;
import com.taobao.api.ApiException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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
    @Value("${dingtalk.agentid}")
    private String agentId;

    private Client eventClient;
    private DingTalkClient noticeClient;

    @PostConstruct
    public void createClient() throws Exception {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config();
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


    String timeFormat = "%sT%s:00+08:00";
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

    private String timeStamp(String text) {
        return text + "\n" + "TOKEN: " + LocalDateTime.now().getNano();
    }

}
