package com.hmall.gateway.fliter;

import com.hmall.common.exception.UnauthorizedException;
import com.hmall.gateway.config.AuthProperties;
import com.hmall.gateway.utils.JwtTool;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MyGlobalFilter implements GlobalFilter, Ordered {

    private final JwtTool jwtTool;

    private final AuthProperties authProperties;

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        if (isExcludePath(request.getPath().toString())) {
            return chain.filter(exchange);
        }
        String token = null;
        Long userId = null;
        List<String> headers = request.getHeaders().get("authorization");
      if (headers != null && !headers.isEmpty()) {

          token = headers.get(0);
      }
      try {
          userId = jwtTool.parseToken(token);
      }catch (UnauthorizedException e){
          ServerHttpResponse response = exchange.getResponse();
          response.setStatusCode(HttpStatus.UNAUTHORIZED);
          return response.setComplete();
      }
        String userInfo = userId.toString();
        ServerWebExchange swe = exchange.mutate().request(builder -> builder.header("user-info", userInfo)).build();
        return chain.filter(swe);
    }

    private boolean isExcludePath(String path) {
        for (String excludePath : authProperties.getExcludePaths()) {
            if (antPathMatcher.match(excludePath, path)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
