package com.se.invigilation.repository;

import com.se.invigilation.dox.Setting;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface SettingRepository extends ReactiveCrudRepository<Setting, String> {

    @Modifying
    @Query("update setting s set s.svalue=:value where s.id=:sid")
    Mono<Integer> update(String sid, String value);
}
