package com.ghost.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class LearningService {

    private final MemoryService memoryService;
    private final ChatModel chatModel; // Pode ser Gemini ou Groq

    @Async // Roda em background para não atrasar a resposta ao usuário
    public void analyzeAndLearn(String userMessage, String aiResponse, String firebaseUid) {
        log.info("Protocolo de Auto-Aprendizado iniciado para usuário {}", firebaseUid);

        String evaluationPrompt = """
            Você é o módulo de memória do GHOST.
            Analise a conversa abaixo e decida se há algo relevante para salvar permanentemente.

            CONVERSA:
            Usuário: %s
            GHOST: %s

            REGRAS:
            - Salve APENAS fatos novos, preferências, detalhes de projetos, decisões ou instruções.
            - Ignore saudações, piadas leves, conversas triviais.
            - Se salvar, atribua importância de 1 a 10.
            - Categoria: personal, work, project, preference, command, other.

            Responda SOMENTE com JSON válido:
            {"shouldSave": true, "content": "resumo curto e preciso do fato", "category": "categoria", "importance": 8}
            ou
            {"shouldSave": false}
            """.formatted(userMessage, aiResponse);

        try {
            String jsonDecision = chatModel.call(new Prompt(evaluationPrompt)).getResult().getOutput().getText();

            // Parse simples (em produção use Jackson/ObjectMapper)
            if (jsonDecision.contains("\"shouldSave\": true")) {
                // Extrai os campos (exemplo básico – melhore com JSON parser)
                String content = extractJsonField(jsonDecision, "content");
                String category = extractJsonField(jsonDecision, "category");
                int importance = Integer.parseInt(extractJsonField(jsonDecision, "importance"));

                memoryService.saveMemory(content, firebaseUid, category, importance);
                log.info("Auto-aprendizado: memória salva (importância {})", importance);
            } else {
                log.debug("Nada relevante para salvar nesta interação.");
            }
        } catch (Exception e) {
            log.error("Erro no auto-aprendizado: {}", e.getMessage(), e);
        }
    }

    // Helper simples (melhore com Jackson em produção)
    private String extractJsonField(String json, String field) {
        String key = "\"" + field + "\":";
        int start = json.indexOf(key) + key.length();
        int end = json.indexOf(",", start);
        if (end == -1) end = json.indexOf("}", start);
        String value = json.substring(start, end).trim();
        return value.replace("\"", "").replace("}", "");
    }
}