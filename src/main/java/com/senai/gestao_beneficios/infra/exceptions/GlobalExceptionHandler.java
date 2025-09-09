package com.senai.gestao_beneficios.infra.exceptions;

import com.senai.gestao_beneficios.DTO.reponsePattern.ApiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Collectors;


@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // ✅ ESTES ESTÃO CORRETOS, DESDE QUE VOCÊ LANCE (throw) ESSAS EXCEÇÕES NO SEU CÓDIGO
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFound(NotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Object>> handleUnauthorized(UnauthorizedException ex) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), ex.getMessage());
    }

    // ❗️ CORRIGIDO: O parâmetro agora é ForbiddenException
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponse<Object>> handleForbidden(ForbiddenException ex) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), ex.getMessage());
    }

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleAlreadyExists(AlreadyExistsException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), ex.getMessage());
    }

    @ExceptionHandler(BadRequest.class)
    public ResponseEntity<ApiResponse<Object>> handleBadRequest(BadRequest ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), ex.getMessage());
    }

    @ExceptionHandler(ServerException.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneric(ServerException ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno", "Ocorreu um erro inesperado.");
    }

    // ❗️ REMOVIDO o seu método handleNotReadable antigo.
    // O método abaixo é a forma correta.

    // ✅ ESTE JÁ ESTAVA CORRETO
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        String errosFormatados = ex.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    return "'" + fieldName + "': " + errorMessage;
                })
                .collect(Collectors.joining("; "));

        ApiResponse<Object> apiResponse = new ApiResponse<>(
                false,
                null,
                errosFormatados,
                null,
                null
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    // ✨ NOVO MÉTODO SOBRESCRITO (A FORMA CORRETA DE TRATAR JSON INVÁLIDO)
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        String mensagemAmigavel = "O corpo da requisição está malformado ou contém um tipo de dado inválido.";

        ApiResponse<Object> apiResponse = new ApiResponse<>(
                false,
                null,
                mensagemAmigavel,
                null,
                null
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }


    // Método auxiliar para construir a resposta padronizada
    private ResponseEntity<ApiResponse<Object>> build(HttpStatus status, String error, String message) {
        ApiResponse<Object> body = new ApiResponse<>(false, null, error, null, message);
        return ResponseEntity.status(status).body(body);
    }
}