package com.ghost.ghost_gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping("/core")
    public ResponseEntity<Map<String, String>> coreFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
            "response", "Senhor Walker, o núcleo neural da IA está sobrecarregado ou inacessível. O disjuntor de segurança foi ativado. Tente novamente em alguns segundos.",
            "status", "FALLBACK_ACTIVATED"
        ));
    }
}