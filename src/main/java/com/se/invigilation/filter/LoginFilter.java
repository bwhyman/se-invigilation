package com.se.invigilation.filter;

import com.se.invigilation.component.JWTComponent;
import com.se.invigilation.exception.Code;
import com.se.invigilation.exception.XException;
import com.se.invigilation.vo.RequestConstant;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
@Order(1)
@RequiredArgsConstructor
public class LoginFilter implements WebFilter {

    private final PathPattern includes = new PathPatternParser().parse("/api/**");
    private final List<PathPattern> excludesS = List.of(new PathPatternParser().parse("/api/login"), new PathPatternParser().parse("/api/l-login"));

    private final JWTComponent jwtComponent;
    private final ResponseHelper responseHelper;

    @NonNull
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        for (PathPattern p : excludesS) {
            if (p.matches(request.getPath().pathWithinApplication())) {
                return chain.filter(exchange);
            }
        }
        if (!includes.matches(request.getPath().pathWithinApplication())) {
            return responseHelper.response(Code.BAD_REQUEST, exchange);
        }
        String token = request.getHeaders().getFirst(RequestConstant.TOKEN);
        if (token == null) {
            return responseHelper.response(Code.UNAUTHORIZED, exchange);
        }

        return jwtComponent.decode(token)
                .flatMap(decode -> {
                    exchange.getAttributes().put(RequestConstant.UID, decode.getClaim(RequestConstant.UID).asString());
                    exchange.getAttributes().put(RequestConstant.ROLE, decode.getClaim(RequestConstant.ROLE).asString());
                    exchange.getAttributes().put(RequestConstant.COLLID, decode.getClaim(RequestConstant.COLLID).asString());
                    exchange.getAttributes().put(RequestConstant.DEPID, decode.getClaim(RequestConstant.DEPID).asString());
                    return chain.filter(exchange);
                })
                .onErrorResume(e -> responseHelper.response(((XException) e).getCode(), exchange));

        //
        /*DecodedJWT decode = jwtComponent.decode(token);
        exchange.getAttributes().put(RequestAttributeConstant.UID, decode.getClaim(RequestAttributeConstant.UID).asString());
        exchange.getAttributes().put(RequestAttributeConstant.ROLE, decode.getClaim(RequestAttributeConstant.ROLE).asString());

        if (!decode.getClaim(RequestAttributeConstant.GROUP_NUMBER).isMissing()) {
            exchange.getAttributes().put(RequestAttributeConstant.GROUP_NUMBER, decode.getClaim(RequestAttributeConstant.GROUP_NUMBER).asInt());
        }
        return chain.filter(exchange);*/

    }
}
