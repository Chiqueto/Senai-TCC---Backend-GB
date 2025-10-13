package com.senai.gestao_beneficios.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API Gestão de Benefícios",
                version = "1.0",
                description = "Documentação da API do projeto de gestão de benefícios."
        ),
        // Aplica o requisito de segurança globalmente a todos os endpoints
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        // Nome de referência para o esquema de segurança
        name = "bearerAuth",
        // Descrição que aparecerá no Swagger UI
        description = "JWT Token de Autenticação",
        // Tipo de esquema
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        // Formato do token
        bearerFormat = "JWT",
        // Onde o token é enviado (neste caso, no Header 'Authorization')
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
    // Esta classe pode ficar vazia, pois as anotações fazem todo o trabalho.
}
