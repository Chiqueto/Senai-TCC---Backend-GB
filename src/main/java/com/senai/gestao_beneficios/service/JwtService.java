package com.senai.gestao_beneficios.service;

import com.senai.gestao_beneficios.domain.colaborador.Colaborador;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {
    private final String secret;
    private final long expirationMillis;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration:28800000}") long expirationMillis) {
        this.secret = secret;
        this.expirationMillis = expirationMillis;
    }

    private Key key() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT secret curto. Para HS256 use >= 32 bytes (ex.: 32+ caracteres ASCII)."
            );
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(UserDetails user) {
        Colaborador c = (Colaborador) user;

        Map<String, Object> claims = Map.of(
                "id", c.getId(),
                "nome", c.getNome(),
                "funcao", c.getFuncao().name(),
                "roles", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList()
        );

        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername()) // matricula
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return parse(token).getBody().getSubject();
    }

    public boolean isValid(String token, UserDetails user) {
        try {
            final Jws<Claims> jws = parse(token);
            final String username = jws.getBody().getSubject();
            final Date exp = jws.getBody().getExpiration();
            return username.equals(user.getUsername()) && exp.after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false; // token inválido/expirado/assinado com chave errada
        }
    }

    private Jws<Claims> parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                // .setAllowedClockSkewSeconds(30) // opcional: tolerância de clock
                .build()
                .parseClaimsJws(token);
    }
}