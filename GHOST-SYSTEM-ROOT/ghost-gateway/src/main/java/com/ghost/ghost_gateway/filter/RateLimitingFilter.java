package com.ghost.ghost_gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@Slf4j
public class RateLimitingFilter implements GlobalFilter, Ordered {

    private final ReactiveStringRedisTemplate redisTemplate;
    private static final int MAX_REQUESTS = 50;

    public RateLimitingFilter(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String ip = exchange.getRequest().getRemoteAddress() != null ? 
                    exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "UNKNOWN";
        String key = "ghost:ratelimit:" + ip;

        // Operações no Redis de forma reativa
        return redisTemplate.opsForValue().increment(key)
                .flatMap(count -> {
                    if (count == 1) {
                        return redisTemplate.expire(key, Duration.ofMinutes(1)).thenReturn(count);
                    }
                    return Mono.just(count);
                })
                .flatMap(count -> {
                    if (count > MAX_REQUESTS) {
                        log.warn("[GHOST FIREWALL] ⚠️ ALERTA: IP {} bloqueado via REDIS. Comandos: {}", ip, count);
                        
                        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
                        
                        byte[] bytes = "{\"status\":\"ERROR\", \"message\":\"Firewall de Elite ativado. Resfriamento necessário (1 minuto).\"}".getBytes();
                        
                        return exchange.getResponse().writeWith(
                            Mono.just(exchange.getResponse().bufferFactory().wrap(bytes))
                        );
                    }
                    // Se estiver dentro do limite, a requisição segue
                    return chain.filter(exchange);
                });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}