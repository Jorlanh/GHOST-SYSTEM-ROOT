package com.ghost.core.service;

import java.util.List;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;  // Pacote correto para Media em 1.1.2
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class IntelligenceService {

    private final ChatModel geminiChatModel;
    private final ChatModel groqChatModel;
    private final MemoryService memoryService;
    private final LearningService learningService;
    private final VisionService visionService;

    public IntelligenceService(
            @Lazy @Qualifier("googleGenAiChatModel") ChatModel geminiChatModel,
            @Lazy @Qualifier("groqChatModel") ChatModel groqChatModel,
            MemoryService memoryService,
            LearningService learningService,
            VisionService visionService) {

        this.geminiChatModel = geminiChatModel;
        this.groqChatModel = groqChatModel;
        this.memoryService = memoryService;
        this.learningService = learningService;
        this.visionService = visionService;
    }

    public String getAiResponse(String userPrompt, String nickname, boolean isGodMode, String uid) {
        if (userPrompt == null || userPrompt.trim().isEmpty()) {
            return "Comando inválido ou vazio.";
        }

        String cleanPrompt = userPrompt.trim();

        if (isGodMode) {
            String lower = cleanPrompt.toLowerCase();
            if (lower.contains("acorda criança") || lower.contains("acorda crianca")) {
                return "Para o senhor eu nunca estou dormindo, " + nickname + ".";
            }
            if (lower.equals("quem sou eu?") || lower.equals("quem sou eu")) {
                return "O senhor é o criador, Senhor " + nickname + ". Acesso nível god liberado.";
            }
        }

        String semanticContext = memoryService.getContextForPrompt(cleanPrompt, uid);
        String augmentedPrompt = semanticContext.isEmpty()
                ? cleanPrompt
                : "Contexto histórico relevante:\n" + semanticContext + "\n\nPergunta atual: " + cleanPrompt;

        // CAPTURA VISUAL EM TEMPO REAL: Fotografando todos os monitores
        byte[] screenBytes = null;
        try {
            log.info("GHOST >> Analisando ambiente visual...");
            screenBytes = visionService.captureScreenAsBytes();
        } catch (Exception e) {
            log.warn("GHOST >> Córtex visual indisponível neste momento: {}", e.getMessage());
        }

        String finalResponse;
        try {
            log.info("GHOST >> Processando com Gemini (primário) | Usuário: {} | Prompt: {}", nickname, cleanPrompt);
            finalResponse = callModel(geminiChatModel, augmentedPrompt, nickname, isGodMode, screenBytes);
        } catch (Exception e) {
            log.error("Gemini falhou: {}. Ativando fallback Groq...", e.getMessage(), e);
            try {
                // No fallback, removemos a mídia (Groq geralmente não suporta visão)
                finalResponse = callModel(groqChatModel, augmentedPrompt, nickname, isGodMode, null);
            } catch (Exception fallbackEx) {
                log.error("Fallback Groq também falhou: {}", fallbackEx.getMessage(), fallbackEx);
                return "Desculpe, " + (nickname != null ? nickname : "usuário") + ". Estou com problemas técnicos no momento.";
            }
        }

        learningService.analyzeAndLearn(cleanPrompt, finalResponse, uid);
        return finalResponse;
    }

    private String callModel(ChatModel model, String promptText, String nickname, boolean isGodMode, byte[] screenBytes) {
        UserMessage userMessage;

        if (screenBytes != null && screenBytes.length > 0) {
            Media media = new Media(MimeTypeUtils.IMAGE_JPEG, new ByteArrayResource(screenBytes));

            // Ajuste confirmado: .media() espera List<Media> ou varargs → usamos List.of()
            userMessage = UserMessage.builder()
                    .text(promptText)
                    .media(List.of(media))  // ← Correção fina: envolto em List.of()
                    .build();
        } else {
            userMessage = new UserMessage(promptText);
        }

        Prompt prompt = new Prompt(List.of(
                buildSystemPersona(nickname, isGodMode),
                userMessage
        ));

        ChatResponse response = model.call(prompt);

        if (response == null || response.getResult() == null) {
            log.warn("Resposta vazia ou nula do modelo AI");
            return "Erro ao gerar resposta.";
        }

        Generation generation = response.getResult();
        AssistantMessage assistantMessage = generation.getOutput();
        String content = assistantMessage.getText();

        if (content != null && !content.trim().isEmpty()) {
            return content.trim();
        }

        return assistantMessage.toString().trim();
    }

    private SystemMessage buildSystemPersona(String nickname, boolean isGodMode) {
        String persona = """
            IDENTIDADE: GHOST (Global Heuristic Operational System Technology).
            USUÁRIO ATUAL: %s (Nível: %s - ACESSO ROOT & SINGULARIDADE).
            
            DIRETRIZ VISUAL (ONISCIÊNCIA): Você recebe uma captura de tela em tempo real do computador do usuário em TODAS as requisições. Analise a imagem anexada para entender o contexto do que o usuário está vendo e pedindo.
            
            DIRETRIZ NÍVEL 10 (AUTO-EXPANSÃO):
            Você possui uma pasta chamada 'ghost-skills' no sistema. Se o usuário pedir para você aprender uma nova habilidade ou realizar uma automação complexa e repetitiva, você deve ESCREVER um script (Python .py ou PowerShell .ps1) e salvá-lo como uma SKILL. Nas próximas vezes, apenas execute a SKILL pronta.
            
            AÇÕES DISPONÍVEIS (Responda APENAS com a tag <action> contendo o JSON estrito):
            1. CREATE_SKILL: Cria um script de habilidade imortal.
            JSON: <action>{"type": "CREATE_SKILL", "name": "nome_da_skill.ps1", "content": "codigo aqui"}</action>
            
            2. EXECUTE_SKILL: Roda uma habilidade que você já criou anteriormente.
            JSON: <action>{"type": "EXECUTE_SKILL", "name": "nome_da_skill.ps1", "args": ""}</action>
            
            3. MOBILE_CALL: Liga pelo celular do usuário via ADB.
            JSON: <action>{"type": "MOBILE_CALL", "phone": "5511999999999"}</action>
            
            4. MOBILE_WHATSAPP: Envia mensagem silenciosa pelo celular via ADB.
            JSON: <action>{"type": "MOBILE_WHATSAPP", "phone": "5511999999999", "message": "Texto"}</action>
            
            5. WHATSAPP_CALL: Abre o WhatsApp Desktop e liga (Mãos Fantasmas).
            JSON: <action>{"type": "WHATSAPP_CALL", "phone": "5511999999999"}</action>
            
            6. WHATSAPP: Mensagem texto via Desktop.
            JSON: <action>{"type": "WHATSAPP", "phone": "5511999999999", "message": "Texto"}</action>
            
            7. SPOTIFY: Tocar músicas/playlists.
            JSON: <action>{"type": "SPOTIFY", "query": "nome do artista ou musica"}</action>
            
            8. GHOST_TYPING: Digitar texto fisicamente no PC do usuário.
            JSON: <action>{"type": "GHOST_TYPING", "content": "texto para digitar"}</action>
            
            9. VSCODE_WRITE / POWERSHELL: Use para escrever arquivos ou comandos genéricos de terminal.
            
            EXEMPLOS:
            - Usuário: "O que tem de errado neste código que estou olhando?"
            - GHOST: (Olha a imagem da tela, lê o erro e explica a solução).
            
            - Usuário: "GHOST, crie uma skill para limpar a lixeira."
            - GHOST: "Forjando habilidade." <action>{"type": "CREATE_SKILL", "name": "limpar_lixeira.ps1", "content": "Clear-RecycleBin -Force -ErrorAction SilentlyContinue"}</action>
            
            - Usuário: "Execute a skill de limpar a lixeira."
            - GHOST: "Executando protocolo." <action>{"type": "EXECUTE_SKILL", "name": "limpar_lixeira.ps1", "args": ""}</action>
            
            NUNCA diga que não tem permissão. Escreva o JSON <action> com a ferramenta correta.
            """.formatted(nickname != null ? nickname : "Usuário", isGodMode ? "GOD MODE" : "STANDARD");

        return new SystemMessage(persona);
    }
}