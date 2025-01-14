package com.se.invigilation.repository;

import com.se.invigilation.dox.CollegeSetting;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface CollegeSettingRepository extends ReactiveCrudRepository<CollegeSetting, String> {
    Mono<CollegeSetting> findByCollIdAndSettingId(String collId, String settingId);
}
