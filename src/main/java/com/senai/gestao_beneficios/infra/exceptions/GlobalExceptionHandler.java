package com.senai.gestao_beneficios.infra.exceptions;

import com.senai.gestao_beneficios.DTO.reponsePattern.ApiResponse;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Collectors;


@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFound(NotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), ex.getMessage());
    }
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Object>> handleUnauthorized(UnauthorizedException ex) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), ex.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponse<Object>> handleForbidden(UnauthorizedException ex) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), ex.getMessage());
    }

    @ExceptionHandler(BadRequest.class)
    public ResponseEntity<ApiResponse<Object>> handleNotReadable(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, (!ex.getMessage().isEmpty()) ? ex.getMessage() :   "JSON malformado ou tipo inválido.", "Dados inválidos.");
    }

    @ExceptionHandler(ServerException.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneric(ServerException ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno", "Ocorreu um erro inesperado.");
    }


    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        // 1. Concatena todas as mensagens de erro em uma única string
        String errosFormatados = ex.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    return "'" + fieldName + "': " + errorMessage; // Formato: 'campo': mensagem
                })
                .collect(Collectors.joining("; ")); // Junta tudo com "; "

        // 2. Cria a instância do SEU ApiResponse com a mensagem formatada
        ApiResponse<Object> apiResponse = new ApiResponse<>(
                false,
                null,
                errosFormatados, // Passa a string com todos os erros
                null, // campo de erros nulo
                null // outros campos
        );

        // 3. Retorna a resposta
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<ApiResponse<Object>> build(HttpStatus status, String error, String message) {
        ApiResponse<Object> body = new ApiResponse<>(false, null, null, null, message);
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
