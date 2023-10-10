package com.se.invigilation.repository;

import com.se.invigilation.dox.Invigilation;
import org.springframework.data.domain.Pageable;
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
            where i.coll_id=:collId and i.status=0
            order by i.date
            """)
    Flux<Invigilation> findimporteds(String collId);

    @Query("""
            select count(i.id) from invigilation i
            where i.department ->> '$.depId'=:depId and i.status in (1,2)
            """)
    Mono<Integer> findIDispatchedTotal(String depid);

    @Query("""
            select * from invigilation i
            where i.department ->> '$.depId'=:depId and i.status in (1,2)
            order by i.date desc limit :#{#pageable.offset}, :#{#pageable.pageSize}
            """)
    Flux<Invigilation> findDispatcheds(String depId, Pageable pageable);

    @Query("""
            select count(i.id) from invigilation i
            where i.department ->> '$.depId'=:depId and i.status=:status
            """)
    Mono<Integer> findTotalByByDepIdAndStatus(String depId, int status);

    @Query("""
            select * from invigilation i
            where i.department ->> '$.depId'=:depId and i.status=:status
            order by i.date desc limit :#{#pageable.offset}, :#{#pageable.pageSize}
            """)
    Flux<Invigilation> findByDepIdAndStatus(String depId, int status, Pageable pageable);

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
            update invigilation iv set iv.calendar_id=:calid, iv.create_union_id=:unionid,
            iv.notice_user_ids=:noticeIds
            where iv.id=:inviid
            """)
    Mono<Integer> updateCalanderId(String inviid, String calid, String unionid, String noticeIds);

    @Modifying
    @Query("""
            update invigilation iv set iv.calendar_id=null, iv.create_union_id=null, iv.notice_user_ids=null
            where iv.id=:inviid
            """)
    Mono<Integer> updateCalanderNull(String inviid);

    Flux<Invigilation> findByCollId(String collId);
}
