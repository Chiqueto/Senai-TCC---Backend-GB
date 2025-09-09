package com.senai.gestao_beneficios.domain.colaborador;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.senai.gestao_beneficios.domain.dependente.Dependente;
import com.senai.gestao_beneficios.domain.solicitacao.Solicitacao;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "colaborador")
@RequiredArgsConstructor
@Getter
@Setter
public class Colaborador implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String matricula;
    @Column(nullable = false)
    private String senha;
    @Column(nullable = false)
    private String nome;
    @Column(nullable = false)
    private LocalDate dtNascimento;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Funcao funcao;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Genero genero;
    private String cidade;
    private Instant created_at;
    private Instant updated_at;

    @OneToMany(orphanRemoval = true, mappedBy = "colaborador", cascade = CascadeType.ALL)
    private List<Solicitacao> solicitacoes;
@OneToMany(
        orphanRemoval = true,
        mappedBy = "colaborador",
        cascade = CascadeType.ALL
)
    private Set<Dependente> dependentes;

    /* --- UserDetails --- */
    @Override @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Ex.: ROLE_GESTAO_BENEFICIOS
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.funcao.name()));
    }
    @Override @JsonIgnore public String getPassword() { return this.senha; }
    @Override @JsonIgnore public String getUsername() { return this.matricula; }
    @Override @JsonIgnore public boolean isAccountNonExpired() { return true; }
    @Override @JsonIgnore public boolean isAccountNonLocked() { return true; }
    @Override @JsonIgnore public boolean isCredentialsNonExpired() { return true; }
    @Override @JsonIgnore public boolean isEnabled() { return true; }
}

