package com.ghost.ghost_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            // 1. Libera o CORS (Permite a conversa com o React/Electron)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 2. Desativa CSRF
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            
            // 3. Regras de Acesso e Blindagem
            .authorizeExchange(auth -> auth
                // Libera o "aperto de mão" do CORS (Preflight OPTIONS)
                .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // BYPASS TÁTICO: Libera explícita e cirurgicamente o texto e a voz
                .pathMatchers("/api/v1/ghost/interact").permitAll() 
                .pathMatchers("/api/v1/ghost/interact/audio").permitAll() 
                .pathMatchers("/actuator/**").permitAll() 
                
                // BLOQUEIO TOTAL
                .anyExchange().authenticated()
            )
            
            // 4. Configura o Gateway para agir como um Validador de Tokens (OAuth2)
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }

    // O Acordo de Paz: Ensina o Gateway a aceitar pedidos vindos da porta do React
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Portas do Vite (8083 ou 5173) e do Electron
        configuration.setAllowedOrigins(List.of("http://localhost:8083", "http://localhost:5173", "electron://localhost"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Atenção: no WebFlux o registro usa a classe reativa
        source.registerCorsConfiguration("/**", configuration); 
        return source;
    }
}