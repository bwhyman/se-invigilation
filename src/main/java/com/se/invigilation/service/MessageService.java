/**
 * Alidayu标准： 必须是标准JSON，数字类型必须包含引号，因此使用string较合适 不能包含[]，因此不能使用集合、数组
 * 所有参数必须赋值，否则报参数缺失，因此即使为空也要声明
 *
 * 手机关机，属于正常发送，即，与正常发送短信效果相同，
 * 当超过运营商限制(不知具体数据)，营运商返回发送失败给dayu，dayu返回发送错误数据，但需单写监听器实现 手机欠费没有测试
 *
 * 短信字数<=70个字数，按照70个字数一条短信计算
 * 短信字数>70个字数，即为长短信，按照67个字数记为一条短信计算
 *
 * 每个参数变量值不能多于15个字符
 */
package com.se.invigilation.service;

import com.se.invigilation.exception.XException;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/*@Service
@Slf4j
public class MessageService {
    @Value("${my.ali.ACCESSKEY_ID}")
    private String ACCESSKEY_ID;
    @Value("${my.ali.ACCESSKEY_SECRET}")
    private String ACCESSKEY_SECRET;
    @Value("${my.ali.SIGN_NAME}")
    private String SIGN_NAME;
    @Value("${my.ali.TEMPLATE_CODE}")
    private String TEMPLATE_CODE;

    private Client client;

    @PostConstruct
    @SneakyThrows
    private void create() {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                .setAccessKeyId(ACCESSKEY_ID)
                .setAccessKeySecret(ACCESSKEY_SECRET);
        config.endpoint = "dysmsapi.aliyuncs.com";
        client = new Client(config);
    }
    *//**
     * 按监考信息发送，而不是监考安排，因为需要为每一位监考教师提供所有监考人员名单，用于在紧急情况下联络相关教师
     * 监考通知模板代码: 第N次申请，为减少短信字符压缩删除参数前的声明，结果已全参数模板被拒 改为自己拼接监考通知字符串
     * 监考:${invi};备注:${comment};地点:${location};人员:${names};分配${freq}次
     * 例子: 65字符
     * [东林软件]监考:15周二06-06 08:00;备注:软件构件与中间件阶段;地点:丹青楼101;人员:王波波,孙哲波;分配15次
     *
     *//*

    @SneakyThrows
    public Mono<String> sendSMS(String phoneNumber,
                                String invi,
                                String comment,
                                String location,
                                String names,
                                String freq) {
        SendSmsRequest sendSmsRequest = new SendSmsRequest();
        sendSmsRequest.setPhoneNumbers(phoneNumber);
        sendSmsRequest.setSignName(SIGN_NAME);
        sendSmsRequest.setTemplateCode(TEMPLATE_CODE);
        String param = """
                {
                  "invi": "%s",
                  "comment": "%s",
                  "location": "%s",
                  "names":" %s",
                  "freq": "%s"
                }
                """;
        sendSmsRequest.setTemplateParam(param.formatted(invi, comment, location, names, freq));
        SendSmsResponse sendSmsResponse = client.sendSms(sendSmsRequest);
        SendSmsResponseBody body = sendSmsResponse.getBody();
        if ("OK".equalsIgnoreCase(body.getCode())) {
            return Mono.just(body.getBizId());
        }
        return Mono.error(XException.builder().codeN(405).message(body.getMessage()).build());
    }
}*/
