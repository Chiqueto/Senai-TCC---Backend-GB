package com.senai.gestao_beneficios.domain.documento;

import com.senai.gestao_beneficios.domain.solicitacao.Solicitacao;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "documentos_enviados")
@Getter
@Setter
@RequiredArgsConstructor
public class Documento {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @ManyToOne
    private Solicitacao solicitacao;
    @Column(unique = true)
    private String nomeArquivoUnico;
    private String nomeArquivoOriginal;
    private long tamanho;
    private String contentType;
    private Instant dataUpload;

    @Enumerated(EnumType.STRING)
    private TipoDocumento tipo; // Identifica o propósito do documento

    private Instant dataAssinatura;
}
