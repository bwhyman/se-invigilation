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
import java.util.List;


@Repository
public interface InvigilationRepository extends ReactiveCrudRepository<Invigilation, String> {

    @Query("""
            select * from invigilation i
            where i.coll_id=:collId and i.status=0
            order by i.date, i.time ->> '$.starttime', i.course ->> '$.courseName', i.course ->> '$.teacherName';
            """)
    Flux<Invigilation> findimporteds(String collId);

    @Query("""
            select count(i.id) from invigilation i
            where i.department ->> '$.depId'=:depId and i.status in (1,2)
            """)
    Mono<Integer> findDispatchedTotal(String depid);

    @Query("""
            select * from invigilation i
            where i.department ->> '$.depId'=:depId and i.status in (1,2)
            order by i.date desc, i.time ->> '$.starttime', i.course ->> '$.courseName', i.course ->> '$.teacherName'
            limit :#{#pageable.offset}, :#{#pageable.pageSize}
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
            order by i.date desc, i.time ->> '$.starttime', i.course ->> '$.courseName', i.course ->> '$.teacherName'
            limit :#{#pageable.offset}, :#{#pageable.pageSize}
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

    @Query("""
            select * from invigilation i
            where i.department ->> '$.depId'=:depId and i.id=:inviid;
            """)
    Mono<Invigilation> findByDepId(String depId, String inviid);

    @Query("""
            select * from invigilation i
            where i.coll_id=:collid and i.id=:inviid;
            """)
    Mono<Invigilation> findByCollId(String collid, String inviid);

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

    @Query("""
            select * from invigilation i
            where i.coll_id=:collid and i.date>=:sdate and i.date<=:edate
            order by i.date, i.time ->> '$.starttime', i.course ->> '$.courseName', i.course ->> '$.teacherName';
            """)
    Flux<Invigilation> findByDate(String collid, String sdate, String edate);

    @Modifying
    @Query("""
            update invigilation i set i.remark=:remark where i.id in (:ids)
            """)
    Mono<Integer> updateRemarks(List<String> ids, String remark);

    @Modifying
    @Query("""
            update invigilation iv set iv.amount=iv.amount-1 where iv.id=:id;
            """)
    Mono<Integer> updateAmount(String id);

    @Modifying
    @Query("""
            delete i, ide from invigilation i left join invi_detail ide
            on i.id=ide.invi_id
            where i.coll_id=:collid
            """)
    Mono<Integer> deleteInvis(String collid);
}
