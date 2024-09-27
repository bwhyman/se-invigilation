package com.se.invigilation.repository;

import com.se.invigilation.dox.InviDetail;
import com.se.invigilation.dto.InviCountDTO;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface InviDetailRepository extends ReactiveCrudRepository<InviDetail, String> {
    @Query("""
            select i.user_id as user_id, u.name, count(i.id) as count
            from invi_detail i join user u
            on i.user_id=u.id
            where u.department ->> '$.depId'=:depid
            group by i.user_id
            order by count
            """)
    Flux<InviCountDTO> findDepUserCounts(String depid);

    @Query("""
            select u.id as user_id, u.account, u.name, u.department ->> '$.departmentName' as department_name, count(i.user_id) as count from user u
            left join invi_detail i on i.user_id=u.id
            where u.department ->> '$.collId'=:collid
            group by i.user_id, u.id, u.name, u.department
            order by u.department ->> '$.depId';
            """)
    Flux<InviCountDTO> findCollUserCounts(String collid);

    Mono<Integer> deleteByInviId(String inviId);
}
