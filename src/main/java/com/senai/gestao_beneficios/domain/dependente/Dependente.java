package com.senai.gestao_beneficios.domain.dependente;

import com.senai.gestao_beneficios.domain.colaborador.Colaborador;
import com.senai.gestao_beneficios.domain.solicitacao.Solicitacao;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "dependente")
@RequiredArgsConstructor
@Getter
@Setter
public class Dependente {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_colaborador", nullable = false)
    private Colaborador colaborador;
    @Column(nullable = false)
    private String nome;

    @OneToMany(orphanRemoval = true, mappedBy = "dependente", cascade = CascadeType.ALL)
    private List<Solicitacao> solicitacoes;
}
