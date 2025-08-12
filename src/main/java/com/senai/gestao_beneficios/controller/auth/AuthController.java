package com.senai.gestao_beneficios.controller.auth;

import com.senai.gestao_beneficios.DTO.login.LoginRequest;
import com.senai.gestao_beneficios.DTO.login.LoginResponse;
import com.senai.gestao_beneficios.domain.colaborador.Colaborador;
import com.senai.gestao_beneficios.repository.ColaboradorRepository;
import com.senai.gestao_beneficios.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager am;
    private final JwtService jwt;
    private final JwtService repo;


    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest req) {
        System.out.println("Iniciou Controller auth");
        try {
            Authentication auth = am.authenticate(
                    new UsernamePasswordAuthenticationToken(req.matricula(), req.senha())
            );
            System.out.println("Passou autenticação: " + auth);

            Colaborador c = (Colaborador) auth.getPrincipal();
            String token = jwt.generateToken(c);
            System.out.println("Gerou token: " + token);

            return ResponseEntity.ok(
                    new LoginResponse(token, c.getId(), c.getNome(), c.getMatricula(), c.getFuncao().name())
            );
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            System.out.println("BadCredentialsException: senha incorreta para " + req.matricula());
            return ResponseEntity.status(401).build();
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            System.out.println("UsernameNotFoundException: " + e.getMessage());
            return ResponseEntity.status(401).build();
        } catch (org.springframework.security.authentication.ProviderNotFoundException e) {
            System.out.println("ProviderNotFoundException: nenhum AuthenticationProvider configurado");
            return ResponseEntity.status(500).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}
