package com.se.invigilation.repository;

import com.se.invigilation.dox.Invigilation;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;


@Repository
public interface InvigilationRepository extends ReactiveCrudRepository<Invigilation, String> {

    @Query("""
            select * from invigilation i
            where i.coll_id=:collId and i.status=:status
            order by i.date
            """)
    Flux<Invigilation> findByCollIdAndStatus(String collId, int status);

    @Query("""
            select * from invigilation i
            where i.department ->> '$.depId'=:depId and i.status=:status
            order by i.date
            """)
    Flux<Invigilation> findByDepIdAndStatus(String depId, int status);

    @Modifying
    @Query("update user u set u.invi_status=:status where u.id=:id")
    Mono<Integer> updateInviStatus(String id, int status);

    @Query("""
            select * from invigilation i
            where i.department ->> '$.depId'=:depId and i.date=:date
            """)
    Flux<Invigilation> findByDepIdAndDate(String depid, LocalDate date);

    @Modifying
    @Query("""
            update invigilation iv set iv.calendar_id=:calid, iv.create_union_id=:unionid where iv.id=:inviid
            """)
    Mono<Integer> updateCalanderId(String inviid, String calid, String unionid);

    @Modifying
    @Query("""
            update invigilation iv set iv.calendar_id=null, iv.create_union_id=null where iv.id=:inviid
            """)
    Mono<Integer> updateCalanderIdNull(String inviid);
}
