package com.se.invigilation.service;

import com.se.invigilation.dox.Invigilation;
import com.se.invigilation.dox.Setting;
import com.se.invigilation.dox.User;
import com.se.invigilation.repository.SettingRepository;
import com.se.invigilation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final DingtalkService dingtalkService;

    public Mono<User> getUser(String account) {
        return userRepository.findByAccount(account);
    }

    public Mono<List<Setting>> getSettings() {
        return settingRepository.findAll().collectList();
    }

    @Transactional
    public Mono<Integer> updatePassword(String uid, String password) {
        return userRepository.updatePasswordById(uid, this.passwordEncoder.encode(password));
    }

    public Mono<List<User>> listUsersDingIds(List<String> ids) {
        return userRepository.findDingIdByIds(ids)
                .collectList();
    }

    public Mono<Integer> cancelInvigilation(Invigilation invi, String userDingIds, String cancelMessage) {
        return dingtalkService.sendNotice(userDingIds, cancelMessage)
                .flatMap((r) -> dingtalkService.deleteCalender(invi.getCreateUnionId(), invi.getCalendarId())).
                thenReturn(1);
    }
    // 获取监考用户
    public Mono<List<User>> listUserDingIdsByInviid(String inviid) {
        return userRepository.findByInviId(inviid).collectList();
    }
}
