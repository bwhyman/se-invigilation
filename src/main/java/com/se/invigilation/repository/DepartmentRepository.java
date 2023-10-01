package com.se.invigilation.repository;

import com.se.invigilation.dox.Department;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface DepartmentRepository extends ReactiveCrudRepository<Department, String> {

    Mono<Department> findByName(String name);

    //@Query("select * from department d where d.college is null")
    Flux<Department> findByCollegeIsNull();

    @Query("""
            select * from department d where d.college ->> '$.collId'=:collId;
            """)
    Flux<Department> findByCollId(String collId);

}
