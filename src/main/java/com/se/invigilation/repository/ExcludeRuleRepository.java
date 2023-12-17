package com.se.invigilation.repository;

import com.se.invigilation.dox.ExcludeRule;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Flux;

@ResponseBody
public interface ExcludeRuleRepository extends ReactiveCrudRepository<ExcludeRule, String> {

    Flux<ExcludeRule> findByDepIdOrderByUserId(String depid);
}
