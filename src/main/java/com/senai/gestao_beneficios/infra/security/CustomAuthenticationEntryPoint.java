package com.senai.gestao_beneficios.infra.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.senai.gestao_beneficios.DTO.reponsePattern.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;

@Component("customAuthenticationEntryPoint")
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {

        // Cria a sua resposta padronizada
        ApiResponse<Object> apiResponse = new ApiResponse<>(
                false,
                null,
                "Acesso não autorizado. É necessário um token de autenticação válido.", // Mensagem de erro principal
                null,
                "Token de autenticação ausente ou inválido no cabeçalho da requisição." // Mensagem detalhada
        );

        // Configura a resposta HTTP
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.UNAUTHORIZED.value()); // Usa 401 Unauthorized, que é mais apropriado

        // Escreve o JSON da resposta no corpo da resposta HTTP
        OutputStream responseStream = response.getOutputStream();
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(responseStream, apiResponse);
        responseStream.flush();
    }
}