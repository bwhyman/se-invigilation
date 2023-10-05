package com.se.invigilation.repository;

import com.se.invigilation.dox.Timetable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface TimetableRepository extends ReactiveCrudRepository<Timetable, String> {

    @Query("""
            select * from timetable t
            where t.dep_id=:depid
            and t.dayweek=:dayweek
            and t.startweek<=:week and t.endweek>=:week
            """)
    Flux<Timetable> findByDepIdAndDate(String depid, int week, int dayweek);

    Mono<Integer> deleteByUserId(String userid);
}
