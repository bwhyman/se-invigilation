package com.se.invigilation.repository;

import com.se.invigilation.dox.User;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
            update user u set u.role=:role where u.account=:account;
            """)
    Mono<Integer> updateRole(String account, String role);
}
