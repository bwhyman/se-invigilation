package com.se.invigilation.repository;

import com.se.invigilation.dox.InviDetail;
import com.se.invigilation.dto.DepartmentAvgDTO;
import com.se.invigilation.dto.InviCountDTO;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface InviDetailRepository extends ReactiveCrudRepository<InviDetail, String> {
    @Query("""
            select u.id as user_id, u.name, count(i.user_id) as count
            from user u left join invi_detail i
            on u.id=i.user_id
            where u.department ->> '$.depId'=:depid
            group by u.id
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

    @Query("""
            select u.department ->> '$.depId' as dep_id, count(u.department ->> '$.depId') as department_quantity
            from user u join invi_detail ivd
            on u.id=ivd.user_id
            where u.department ->> '$.collId'=:collid and u.invi_status=1
            group by u.department ->> '$.depId'
            """)
    Flux<DepartmentAvgDTO> findInviQuantityByCollId(String collid);
}
