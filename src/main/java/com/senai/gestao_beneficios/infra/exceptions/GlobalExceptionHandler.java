package com.senai.gestao_beneficios.infra.exceptions;

import com.senai.gestao_beneficios.DTO.reponsePattern.ApiError;
import com.senai.gestao_beneficios.DTO.reponsePattern.ApiResponse;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFound(NotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, new ApiError("404", ex.getMessage()), ex.getMessage());
    }
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Object>> handleUnauthorized(UnauthorizedException ex) {
        return build(HttpStatus.UNAUTHORIZED, new ApiError("401", ex.getMessage()), ex.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponse<Object>> handleForbidden(UnauthorizedException ex) {
        return build(HttpStatus.FORBIDDEN, new ApiError("403", ex.getMessage()), ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotReadable(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, new ApiError("400", "JSON malformado ou tipo inválido."), "Dados inválidos.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneric(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, new ApiError("500", "Erro interno"), "Ocorreu um erro inesperado.");
    }

    private ResponseEntity<ApiResponse<Object>> build(HttpStatus status, ApiError error, String message) {
        ApiResponse<Object> body = new ApiResponse<>(false, null, error, null, message);
        return ResponseEntity.status(status).body(body);
    }

    private String formatFieldError(FieldError fe) {
        String field = fe.getField();
        String msg = fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "inválido";
        Object rejected = fe.getRejectedValue();
        return rejected == null
                ? "%s: %s".formatted(field, msg)
                : "%s: %s (valor: %s)".formatted(field, msg, rejected);
    }

    private String mostSpecificMessage(DataAccessException ex) {
        Throwable most = ex.getMostSpecificCause(); // agora compila
        String msg = (most != null && most.getMessage() != null) ? most.getMessage() : ex.getMessage();
        return (msg != null) ? msg : "violação de integridade";
    }
}
