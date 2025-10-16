package com.senai.gestao_beneficios.service.chat;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatHistoryService {

    // Usamos ConcurrentHashMap para segurança em ambientes com múltiplas threads.
    private final Map<String, List<Map<String, Object>>> conversationHistories = new ConcurrentHashMap<>();

    public List<Map<String, Object>> getHistory(String conversationId) {
        // Retorna uma cópia da lista para evitar modificações externas inesperadas.
        return new ArrayList<>(conversationHistories.getOrDefault(conversationId, new ArrayList<>()));
    }

    public void addMessage(String conversationId, Map<String, Object> message) {
        // Garante que a lista exista antes de adicionar uma mensagem.
        conversationHistories.computeIfAbsent(conversationId, k -> new ArrayList<>()).add(message);
    }

    public void saveHistory(String conversationId, List<Map<String, Object>> messages) {
        conversationHistories.put(conversationId, messages);
    }
}