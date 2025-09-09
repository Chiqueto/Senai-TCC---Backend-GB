package com.senai.gestao_beneficios.domain.beneficio;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;

@Entity
@Table(name = "beneficio")
@RequiredArgsConstructor
@Getter
@Setter
public class Beneficio {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public String id;
    @Column(nullable = false)
    public String nome;
    @Column(nullable = false)
    public String descricao;
    @CreatedDate
    public Instant created_at;
}
