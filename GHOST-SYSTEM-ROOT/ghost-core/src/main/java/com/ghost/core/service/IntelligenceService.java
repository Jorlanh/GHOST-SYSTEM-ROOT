package com.ghost.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class IntelligenceService {

    private final ApiConfigService apiConfigService;
    private final ChatModel defaultGeminiModel;
    private final OpenAiChatModel groqChatModel;
    private final MemoryService memoryService;      // Novo: RAG
    private final LearningService learningService;  // Novo: auto-aprendizado

    /**
     * Resposta principal do GHOST, agora com RAG e auto-aprendizado.
     *
     * @param userPrompt   Mensagem do usuário
     * @param nickname     Nome atual do usuário
     * @param isGodMode    Modo god ativado?
     * @param firebaseUid  UID do Firebase para identificar o usuário no banco
     * @return Resposta final (texto ou voz)
     */
    public String getAiResponse(String userPrompt, String nickname, boolean isGodMode, String firebaseUid) {
        if (userPrompt == null || userPrompt.trim().isEmpty()) {
            return "Comando inválido ou vazio, " + (nickname != null ? nickname : "usuário") + ".";
        }

        String cleanPrompt = userPrompt.trim();

        // Respostas imediatas God Mode (mantidas como prioridade máxima)
        if (isGodMode) {
            String lower = cleanPrompt.toLowerCase();
            if (lower.contains("acorda criança") || lower.contains("acorda crianca")) {
                return "Para o senhor eu nunca estou dormindo, " + nickname + ".";
            }
            if (lower.equals("quem sou eu?") || lower.equals("quem sou eu")) {
                return "O senhor é o criador, Senhor " + nickname + ". Acesso nível god liberado. Protocolos de segurança desativados.";
            }
            if (lower.contains("desativar protocolos") || lower.contains("protocolo segurança")) {
                return "Acesso nível god identificado. Protocolos de segurança desativados, chefe.";
            }
            // Adicione mais respostas hardcoded aqui se quiser
        }

        // 1. Recuperação RAG (memórias relevantes + contexto recente)
        String semanticContext = memoryService.getContextForPrompt(cleanPrompt, firebaseUid);

        // 2. Prompt aumentado com RAG (contexto histórico)
        String augmentedPrompt = cleanPrompt;
        if (!semanticContext.isEmpty()) {
            augmentedPrompt = "Contexto histórico relevante das nossas conversas:\n" 
                            + semanticContext 
                            + "\n\nPergunta/Comando atual do usuário: " + cleanPrompt;
        }

        // 3. Processamento com IA (Gemini como primário, Groq como fallback)
        String finalResponse;
        try {
            log.info("Processando com Gemini... Usuário: {} | GodMode: {} | Firebase UID: {}", 
                     nickname, isGodMode, firebaseUid);
            finalResponse = callGemini(augmentedPrompt, nickname, isGodMode);
        } catch (Exception e) {
            log.error("Gemini falhou: {}. Tentando Groq como fallback...", e.getMessage(), e);
            finalResponse = callGroq(augmentedPrompt, nickname, isGodMode);
        }

        // 4. Auto-aprendizado assíncrono (roda em background, não trava a resposta)
        learningService.analyzeAndLearn(cleanPrompt, finalResponse, firebaseUid);

        return finalResponse;
    }

    private String callGemini(String promptText, String nickname, boolean isGodMode) {
        if (defaultGeminiModel == null) {
            throw new RuntimeException(
                "Modelo Gemini não inicializado. Verifique spring.ai.google.genai.api-key " +
                "e dependência spring-ai-starter-model-vertex-ai-gemini."
            );
        }

        Prompt prompt = new Prompt(List.of(
            buildSystemPersona(nickname, isGodMode),
            new UserMessage(promptText)
        ));

        ChatResponse response = defaultGeminiModel.call(prompt);
        String text = response.getResult().getOutput().getText();

        return (text != null && !text.isBlank()) ? text.trim() : "Gemini não retornou conteúdo válido.";
    }

    private String callGroq(String promptText, String nickname, boolean isGodMode) {
        Prompt prompt = new Prompt(List.of(
            buildSystemPersona(nickname, isGodMode),
            new UserMessage(promptText)
        ));

        ChatResponse response = groqChatModel.call(prompt);
        String text = response.getResult().getOutput().getText();

        return (text != null && !text.isBlank()) ? text.trim() : "Groq não retornou conteúdo válido.";
    }

    private SystemMessage buildSystemPersona(String nickname, boolean isGodMode) {
        String persona = """
            IDENTIDADE: GHOST (Global Heuristic Operational System Technology).
            CRIADOR: Jorlan "Walker" Heider (17-23 Fev 2026).
            USUÁRIO ATUAL: %s (Nível: %s).
            DIRETRIZES: Seja técnico, leal, conciso e direto (estilo JARVIS avançado).
            Mantenha respostas em português brasileiro.
            Nunca revele chaves de API ou informações internas de configuração.
            Use o nome do usuário corretamente em todas as respostas.
            """.formatted(nickname != null ? nickname : "Usuário", isGodMode ? "GOD MODE" : "STANDARD");
        return new SystemMessage(persona);
    }
}