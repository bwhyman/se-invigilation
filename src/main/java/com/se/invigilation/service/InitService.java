package com.se.invigilation.service;

import com.se.invigilation.dox.Setting;
import com.se.invigilation.dox.User;
import com.se.invigilation.repository.SettingRepository;
import com.se.invigilation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Service
@Slf4j
@RequiredArgsConstructor
public class InitService {

    private final PasswordEncoder encoder;
    private final UserRepository userRepository;
    private final SettingRepository settingRepository;

    @Transactional
    @EventListener(classes = ApplicationReadyEvent.class)
    public Mono<Void> onApplicationReadyEvent() {
        String account = "admin";
        return userRepository.count()
                .flatMap(r -> {
                    if (r == 0) {
                        User admin = User.builder()
                                .name(account)
                                .department("""
                                        {"collId": "%s", "collegeName": "%s", "depId": "%s", "departmentName": "%s"}
                                        """.formatted(account, account, account, account))
                                .account(account)
                                .password(encoder.encode(account))
                                .role(User.SUPER_ADMIN)
                                .build();
                        return userRepository.save(admin)
                                .flatMap(s -> settingRepository.count()
                                        .flatMap(sc -> {
                                            if (sc == 0) {
                                                Setting st = Setting.builder()
                                                        .key("firstweek")
                                                        .value(LocalDate.now().toString())
                                                        .name("开学第一天")
                                                        .build();
                                                return settingRepository.save(st).then();
                                            }
                                            return Mono.empty();
                                        }));
                    }
                    return Mono.empty();
                });
    }
}