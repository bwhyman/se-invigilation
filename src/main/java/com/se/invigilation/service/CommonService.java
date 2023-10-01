package com.se.invigilation.service;

import com.se.invigilation.dox.Setting;
import com.se.invigilation.repository.SettingRepository;
import com.se.invigilation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommonService {
    private final SettingRepository settingRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Mono<List<Setting>> getSettings() {
        return settingRepository.findAll().collectList();
    }

    @Transactional
    public Mono<Integer> updatePassword(String uid, String password) {
        return userRepository.updatePasswordById(uid, passwordEncoder.encode(password));
    }
}
