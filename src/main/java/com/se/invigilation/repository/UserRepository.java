package com.se.invigilation.repository;

import com.se.invigilation.dox.User;
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
            select * from user u where u.department ->> '$.depId'=:depid and u.role=:role;
            """)
    Flux<User> findByDepidAndrole(String depid, String role);

    @Query("""
            select * from user u where u.department ->> '$.depId'=:depid and u.name=:name;
            """)
    Flux<User> findByDepIdAndName(String depid, String name);

    @Modifying
    @Query("""
            update user u set u.role=:role where u.id=:uid;
            """)
    Mono<Integer> updateRole(String uid, String role);

    @Modifying
    @Query("""
            update user u set u.department=:depart where u.id=:uid
            """)
    Mono<Integer> updateDepartment(String uid, String depart);

    @Modifying
    @Query("""
            update user u set u.password=:password where u.account=:account;
            """)
    Mono<Integer> updatePassword(String account, String password);

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

    @Query("""
            select * from user u
            where u.department ->> '$.collId'=:collid and u.name=:name;
            """)
    Flux<User> findByName(String collid, String name);
}
