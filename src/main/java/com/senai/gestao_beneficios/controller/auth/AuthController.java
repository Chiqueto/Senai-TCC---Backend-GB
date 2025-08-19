package com.senai.gestao_beneficios.controller.auth;

import com.senai.gestao_beneficios.DTO.login.LoginRequest;
import com.senai.gestao_beneficios.DTO.login.LoginResponse;
import com.senai.gestao_beneficios.domain.colaborador.Colaborador;
import com.senai.gestao_beneficios.repository.ColaboradorRepository;
import com.senai.gestao_beneficios.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Autenticação", description = "Endpoints para gerenciamento de autenticação")
public class AuthController {
    private final AuthenticationManager am;
    private final JwtService jwt;
    private final JwtService repo;


    @PostMapping("/login")
    @Operation(
            summary = "Realiza a autenticação de um colaborador",
            description = "Autentica o colaborador com base na matrícula e senha. Se as credenciais forem válidas, retorna um token JWT e os dados básicos do usuário."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Autenticação bem-sucedida",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autorizado. Matrícula ou senha inválidas",
                    content = @Content // Corpo da resposta vazio
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno no servidor",
                    content = @Content // Corpo da resposta vazio
            )
    })
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest req) {
        System.out.println("Iniciou Controller auth");
        try {
            Authentication auth = am.authenticate(
                    new UsernamePasswordAuthenticationToken(req.matricula(), req.senha())
            );

            Colaborador c = (Colaborador) auth.getPrincipal();
            String token = jwt.generateToken(c);

            return ResponseEntity.ok(
                    new LoginResponse(token, c.getId(), c.getNome(), c.getMatricula(), c.getFuncao().name())
            );
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            return ResponseEntity.status(401).build();
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            return ResponseEntity.status(401).build();
        } catch (org.springframework.security.authentication.ProviderNotFoundException e) {
            return ResponseEntity.status(500).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}
