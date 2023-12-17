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
            order by count desc
            """)
    Flux<InviCountDTO> findDepUserCounts(String depid);

    @Query("""
            select i.user_id, count(i.id) as count
            from invi_detail i join invigilation inv
            on i.invi_id=inv.id
            where inv.coll_id=:collid
            group by i.user_id
            """)
    Flux<InviCountDTO> findCollUserCounts(String collid);

    Mono<Integer> deleteByInviId(String inviId);
}
