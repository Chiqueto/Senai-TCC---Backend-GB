package com.senai.gestao_beneficios.domain.conversa;

import com.senai.gestao_beneficios.domain.colaborador.Colaborador;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.Map;

@Entity
@Table(name = "conversa_chat")
@Getter
@Setter
public class Conversa {

    @Id
    private String id; // Este será o seu 'conversationId'

    // Esta coluna vai armazenar a lista de mensagens como um JSON no banco.
    // A anotação @JdbcTypeCode diz ao Hibernate como lidar com a conversão.
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<Map<String, Object>> historico;

     @ManyToOne
     private Colaborador colaborador;
}