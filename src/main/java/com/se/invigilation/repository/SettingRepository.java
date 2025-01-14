package com.se.invigilation.repository;

import com.se.invigilation.dox.Setting;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface SettingRepository extends ReactiveCrudRepository<Setting, String> {

    @Modifying
    @Query("update setting s set s.svalue=:value where s.id=:sid")
    Mono<Integer> update(String sid, String value);

    Flux<Setting> findByState(int state);

    @Query("""
            select s.id, s.skey, coalesce(cs.svalue, s.svalue) as svalue
            from setting s left join college_setting cs
            on s.id=cs.setting_id and cs.coll_id=:collid;
            """)
    Flux<Setting> findByCollId(String collid);
}
