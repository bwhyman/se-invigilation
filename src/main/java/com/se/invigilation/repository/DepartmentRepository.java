package com.se.invigilation.repository;

import com.se.invigilation.dox.Department;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface DepartmentRepository extends ReactiveCrudRepository<Department, String> {

    @Query("select * from department d where d.college ->> '$.collId'=:collid and d.name=:name")
    Mono<Department> findByCollIdAndName(String collid, String name);

    Flux<Department> findByRoot(int root);

    @Query("select * from department d where d.college ->> '$.collId'=:collId")
    Flux<Department> findByCollId(String collId);

    @Query("select * from department d where d.college ->> '$.collId'=:collId and d.invi_status=:status")
    Flux<Department> findByCollId(String collId, int status);

    @Modifying
    @Query("update department d set d.invi_status=:status where d.id=:depid")
    Mono<Integer> updateInviStatusById(String depid, int status);

    @Modifying
    @Query("""
            update department d set d.comment=:comment where d.id=:depid;
            """)
    Mono<Integer> updateComment(String depid, String comment);

    @Query("select d.comment from department d where d.id=:deipd;")
    Mono<String> findCommentByDepid(String deipd);

    @Modifying
    @Query("update department d set d.name=:name where d.id=:depId and d.college ->> '$.collId'=:collId")
    Mono<Integer> updateName(String depId, String collId, String name);
}
