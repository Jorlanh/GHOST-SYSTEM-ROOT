package com.ghost.core.config;

import com.ghost.core.service.ApiConfigService;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class AiConfig {

    private final ApiConfigService apiConfigService;

    public AiConfig(ApiConfigService apiConfigService) {
        this.apiConfigService = apiConfigService;
    }

    @Bean
    public OpenAiChatModel groqChatModel(RestClient.Builder restClientBuilder) {
        // Busca a chave no banco/redis ao iniciar a aplicação
        // Se falhar aqui, o app não sobe (o que é bom, pois avisa do erro cedo)
        // Se preferir que suba mesmo sem chave, envolva em try-catch e retorne null (não recomendado)
        String apiKey = apiConfigService.getApiKey("GROQ_LLAMA");

        if (apiKey == null || apiKey.trim().isEmpty()) {
             // Fallback temporário para não quebrar o boot se o banco estiver vazio na primeira vez
             System.err.println("AVISO: Chave GROQ_LLAMA não encontrada. O fallback para Groq não funcionará.");
             apiKey = "dummy_key_to_allow_startup"; 
        }

        // Configuração da API do Groq
        OpenAiApi groqApi = OpenAiApi.builder()
            .baseUrl("https://api.groq.com/openai/v1")
            .apiKey(apiKey)
            .restClientBuilder(restClientBuilder)
            .build();

        // Opções padrão do modelo
        OpenAiChatOptions options = OpenAiChatOptions.builder()
            .model("llama3-70b-8192")
            .temperature(0.6)
            .build();

        return OpenAiChatModel.builder()
            .openAiApi(groqApi)
            .defaultOptions(options)
            .build();
    }
}