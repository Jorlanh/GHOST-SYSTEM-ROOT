package com.ghost.ghost_gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AuditLoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();
        
        // Em WebFlux, pegamos os dados pelo exchange
        String ip = exchange.getRequest().getRemoteAddress() != null ? 
                    exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "UNKNOWN";
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();

        log.info("[GHOST AUDIT] 🟢 RASTREIO INICIADO | IP: {} | Rota: {} {}", ip, method, path);

        // O 'then' garante que o log final só seja escrito após a IA responder
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long duration = System.currentTimeMillis() - startTime;
            int status = exchange.getResponse().getStatusCode() != null ? 
                         exchange.getResponse().getStatusCode().value() : 200;
            log.info("[GHOST AUDIT] 🔴 RASTREIO CONCLUÍDO | Status: {} | Latência: {}ms", status, duration);
        }));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}