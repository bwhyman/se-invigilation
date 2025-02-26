package com.se.invigilation.repository;

import com.se.invigilation.dox.User;
import com.se.invigilation.dto.DepartmentAvgDTO;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface UserRepository extends ReactiveCrudRepository<User, String> {

    Mono<User> findByAccount(String account);
    @Query("""
            select * from user u where u.department ->> '$.collId'=:collid;
            """)
    Flux<User> findByCollId(String collid);

    @Query("""
            select * from user u where u.department ->> '$.depId'=:depid
            """)
    Flux<User> findByDepId(String depid);

    @Modifying
    @Query("""
            update user u set u.password=:password where u.id=:uid;
            """)
    Mono<Integer> updatePasswordById(String uid, String password);

    @Query("""
            select * from user u where u.department ->> '$.depId'=:depid and u.department ->> '$.collId'=:collid and u.role=:role;
            """)
    Flux<User> findByDepidAndrole(String depid, String collid, String role);

    @Modifying
    @Query("""
            update user u set u.department=json_set(u.department, '$.departmentName', :name)
            where u.department ->> '$.depId'=:depid and u.department ->> '$.collId'=:collid;
            """)
    Mono<Integer> updateUsersDepartment(String depid, String collid, String name);

    @Modifying
    @Query("""
            update user u set u.password=:password where u.account=:account and u.department ->> '$.collId'=:collid;
            """)
    Mono<Integer> updatePassword(String account, String collid, String password);

    @Query("""
            select u.ding_user_id, u.ding_union_id,u.id from user u where u.id in (:ids) group by u.id;
            """)
    Flux<User> findDingIdByIds(List<String> ids);

    @Query("""
            select u.id as id, u.name as name, u.ding_user_id as ding_user_id, u.ding_union_id as ding_union_id
            from invi_detail i, user u
            where i.invi_id=:inviid and i.user_id=u.id group by u.id;
            """)
    Flux<User> findByInviId(String inviid);

    @Modifying
    @Query("""
            delete from user u where u.id=:id and u.department ->> '$.collId'=:collid;
            """)
    Mono<Integer> deleteById(String uid, String collid);

    @Query("""
            select * from user u where u.id=:id and u.department ->> '$.collId'=:collid;
            """)
    Mono<User> findByCollId(String id, String collid);

    @Query("""
             select u.department ->> '$.depId' as dep_id, count(u.department ->> '$.depId') as teacher_quantity from user u
             where u.department ->> '$.collId'=:collid and u.invi_status=1
             group by u.department ->> '$.depId'
            """)
    Flux<DepartmentAvgDTO> findTeacherQuantityByCollId(String collid);
}
