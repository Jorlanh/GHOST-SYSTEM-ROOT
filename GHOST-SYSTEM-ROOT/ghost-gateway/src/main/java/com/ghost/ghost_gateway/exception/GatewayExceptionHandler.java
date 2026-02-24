package com.ghost.ghost_gateway.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import java.net.ConnectException;
import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GatewayExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex, ServerWebExchange exchange) {
        // No WebFlux, usamos o exchange para extrair os dados da requisição
        String path = exchange.getRequest().getURI().getPath();
        
        log.error("[GHOST SHIELD] 🛡️ Falha interceptada na rota [{}]: {}", path, ex.getMessage());

        HttpStatus status = HttpStatus.SERVICE_UNAVAILABLE;
        String message = "O núcleo GHOST está indisponível no momento. O disjuntor de segurança conteve a falha.";

        // Trata erros específicos de roteamento do Gateway WebFlux
        if (ex instanceof ResponseStatusException responseStatusException) {
            status = HttpStatus.valueOf(responseStatusException.getStatusCode().value());
            if (status == HttpStatus.NOT_FOUND) {
                message = "Rota neural não encontrada. Verifique se o microserviço de destino está online no Eureka.";
            }
        } else if (ex instanceof ConnectException) {
            message = "Conexão recusada. O microserviço de destino parece estar offline.";
        }

        return ResponseEntity.status(status).body(Map.of(
            "status", "ERROR",
            "code", status.value(),
            "message", message,
            "path", path,
            "timestamp", LocalDateTime.now().toString()
        ));
    }
}