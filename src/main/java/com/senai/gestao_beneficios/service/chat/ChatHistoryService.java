package com.senai.gestao_beneficios.service.chat;

import com.senai.gestao_beneficios.domain.colaborador.Colaborador;
import com.senai.gestao_beneficios.domain.conversa.Conversa;
import com.senai.gestao_beneficios.repository.ConversaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@AllArgsConstructor
@Service
public class ChatHistoryService {

    private final ConversaRepository conversaRepository;

    // Usamos ConcurrentHashMap para segurança em ambientes com múltiplas threads.
    private final Map<String, List<Map<String, Object>>> conversationHistories = new ConcurrentHashMap<>();

    public List<Map<String, Object>> getHistory(String conversationId) {
        Optional<Conversa> conversaOptional = conversaRepository.findById(conversationId);

        return conversaOptional.map(Conversa::getHistorico).orElse(new ArrayList<>());
    }

    public void addMessage(String conversationId, Map<String, Object> message) {
        // Garante que a lista exista antes de adicionar uma mensagem.
        conversationHistories.computeIfAbsent(conversationId, k -> new ArrayList<>()).add(message);
    }

    public void saveHistory(String conversationId, List<Map<String, Object>> messages, Colaborador colaborador) {
        Conversa conversa = conversaRepository.findById(conversationId)
                .orElse(new Conversa());

        conversa.setId(conversationId);
        conversa.setHistorico(messages);
        conversa.setColaborador(colaborador);

        conversaRepository.save(conversa);
    }
}