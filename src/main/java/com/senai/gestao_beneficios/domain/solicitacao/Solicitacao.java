package com.senai.gestao_beneficios.domain.solicitacao;


import com.senai.gestao_beneficios.domain.beneficio.Beneficio;
import com.senai.gestao_beneficios.domain.colaborador.Colaborador;
import com.senai.gestao_beneficios.domain.dependente.Dependente;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

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
    @Enumerated(EnumType.STRING)
    public TipoPagamento tipoPagamento;
    @Enumerated(EnumType.STRING)
    public StatusSolicitacao status;
}
