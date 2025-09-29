package com.senai.gestao_beneficios.infra.exceptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.senai.gestao_beneficios.DTO.reponsePattern.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component // Marca como um bean do Spring
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public CustomAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {

        // 1. Define o status HTTP correto para Acesso Negado
        response.setStatus(HttpStatus.FORBIDDEN.value()); // 403
        response.setContentType("application/json;charset=UTF-8");

        // 2. Cria o corpo da resposta usando seu padrão ApiResponse
        ApiResponse<Object> apiResponse = new ApiResponse<>(
                false,
                null,
                "Acesso Negado. Você não tem permissão para executar esta ação.", // Mensagem de erro clara
                null,
                null
        );

        // 3. Escreve a resposta JSON
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}