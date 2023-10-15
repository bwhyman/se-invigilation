package com.se.invigilation.repository;

import com.se.invigilation.dox.InviDetail;
import com.se.invigilation.dox.User;
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


    @Query("""
            select u.id as id, u.name as name, u.ding_user_id as ding_user_id, u.ding_union_id as ding_union_id
            from invi_detail i, user u
            where i.invi_id=:inviid and i.user_id=u.id group by u.id;
            """)
    Flux<User> findByInviId(String inviid);

    Mono<Integer> deleteByInviId(String inviId);
}
