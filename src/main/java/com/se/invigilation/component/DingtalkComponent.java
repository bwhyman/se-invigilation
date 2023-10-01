package com.se.invigilation.component;

import com.aliyun.dingtalkoauth2_1_0.Client;
import com.aliyun.dingtalkoauth2_1_0.models.GetAccessTokenRequest;
import com.aliyun.dingtalkoauth2_1_0.models.GetAccessTokenResponse;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
@Slf4j
@RequiredArgsConstructor
@Getter
public class DingtalkComponent {
    @Value("${dingtalk.appkey}")
    private String appKey;
    @Value("${dingtalk.appsecret}")
    private String appSecret;

    private Client client;

    @PostConstruct
    private void createClient() {
        Config config = new Config();
        config.protocol = "https";
        config.regionId = "central";
        try {
            client = new Client(config);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String cacheToken;
    private LocalDateTime getTokenTime;

    public String getDingtalkToken() {
        if (getTokenTime != null && getTokenTime.until(LocalDateTime.now(), ChronoUnit.MINUTES) < 115) {
            return cacheToken;
        }
        GetAccessTokenRequest getAccessTokenRequest = new GetAccessTokenRequest()
                .setAppKey(appKey)
                .setAppSecret(appSecret);
        try {
            GetAccessTokenResponse x = client.getAccessToken(getAccessTokenRequest);
            String token = x.body.getAccessToken();
            log.debug("dingtalktoken: {}", token);
            getTokenTime = LocalDateTime.now();
            cacheToken = token;
            return token;
        } catch (TeaException err) {
            log.debug("{}/{}", err.getCode(), err.getMessage());

        } catch (Exception _err) {
            TeaException err = new TeaException(_err.getMessage(), _err);
            log.debug("{}/{}", err.getCode(), err.getMessage());
        }
        return "";
    }
}
