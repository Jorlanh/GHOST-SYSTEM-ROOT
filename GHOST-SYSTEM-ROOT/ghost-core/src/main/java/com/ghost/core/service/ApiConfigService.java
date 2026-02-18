package com.ghost.core.service;

import com.ghost.core.model.ApiConfig;
import com.ghost.core.repository.ApiConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class ApiConfigService {

    @Autowired
    private ApiConfigRepository repository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String REDIS_KEY_PREFIX = "ghost:api:";

    public String getApiKey(String serviceName) {
        String cacheKey = REDIS_KEY_PREFIX + serviceName.toUpperCase(); // normaliza para evitar duplicatas
        String cachedKey = redisTemplate.opsForValue().get(cacheKey);

        if (cachedKey != null) {
            return cachedKey;
        }

        return repository.findByServiceName(serviceName.toUpperCase())
            .map(config -> {
                String key = config.getApiKey();
                if (config.getIsActive() != null && !config.getIsActive()) {
                    throw new IllegalStateException("Configuração inativa para: " + serviceName);
                }
                redisTemplate.opsForValue().set(cacheKey, key, 1, TimeUnit.HOURS);
                return key;
            })
            .orElseThrow(() -> new RuntimeException("API Key não encontrada para o serviço: " + serviceName));
    }

    public void refreshCache() {
        Set<String> keys = redisTemplate.keys(REDIS_KEY_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    // Opcional: método para forçar reload de uma chave específica
    public void invalidateCache(String serviceName) {
        redisTemplate.delete(REDIS_KEY_PREFIX + serviceName.toUpperCase());
    }
}