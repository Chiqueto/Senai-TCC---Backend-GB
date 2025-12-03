package com.senai.gestao_beneficios.domain.solicitacao;


import com.senai.gestao_beneficios.domain.beneficio.Beneficio;
import com.senai.gestao_beneficios.domain.colaborador.Colaborador;
import com.senai.gestao_beneficios.domain.dependente.Dependente;
import com.senai.gestao_beneficios.domain.documento.Documento;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "solicitacao_beneficio")
@Getter
@Setter
@RequiredArgsConstructor
public class Solicitacao {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public String id;
    @ManyToOne
    public Colaborador colaborador;
    @ManyToOne
    public Dependente dependente;
    @ManyToOne
    public Beneficio beneficio;
    public BigDecimal valorTotal;
    public BigDecimal desconto;
    public String descricao;
    public Integer qtdeParcelas;
    public Instant dataSolicitacao;
    public String justificativa;
    @Enumerated(EnumType.STRING)
    public TipoPagamento tipoPagamento;
    @Enumerated(EnumType.STRING)
    public StatusSolicitacao status;
    @OneToMany(mappedBy = "solicitacao")
    public List<Documento> documentos;
}
