package com.senai.gestao_beneficios.domain.dependente;

import com.senai.gestao_beneficios.domain.colaborador.Colaborador;
import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "dependente")
@RequiredArgsConstructor
public class Dependente {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_colaborador", nullable = false)
    private Colaborador colaborador;
    @Column(nullable = false)
    private String nome;
}
