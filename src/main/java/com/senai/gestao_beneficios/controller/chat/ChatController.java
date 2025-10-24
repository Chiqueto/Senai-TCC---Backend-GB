package com.senai.gestao_beneficios.controller.chat;

import com.senai.gestao_beneficios.DTO.beneficio.BeneficioRequestDTO;
import com.senai.gestao_beneficios.DTO.beneficio.BeneficioResponseDTO;
import com.senai.gestao_beneficios.DTO.chat.ChatRequestDTO;
import com.senai.gestao_beneficios.DTO.chat.ChatResponseDTO;
import com.senai.gestao_beneficios.DTO.reponsePattern.ApiResponse;
import com.senai.gestao_beneficios.service.chat.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    @PostMapping("")
    @Operation(
            summary = "Envia uma mensagem para o chat",
            description = "Endpoint principal para a conversa com o chatbot. Envie uma mensagem e, se aplicável, o ID da conversa para dar continuidade."
    )
    @ApiResponses(value = {
            // CORREÇÃO: Usar 200 OK para uma resposta de sucesso que retorna conteúdo. 201 é para criação de recurso.
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Mensagem retornada com sucesso!",
                    content = @Content(
                            mediaType = "application/json",
                            // CORREÇÃO: O schema deve corresponder ao objeto de resposta real.
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "Requisição inválida", content = @Content
            ),
    })
    public ResponseEntity<ApiResponse<ChatResponseDTO>> getChatMessage (@RequestBody @Valid ChatRequestDTO request) throws Exception {
        ApiResponse<ChatResponseDTO> response = chatService.getChatResponse(request);
        // Simplificação: ResponseEntity.ok() é um atalho para status(HttpStatus.OK).body()
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Finaliza um fluxo de chat com o upload de um documento",
            description = "Este endpoint é usado quando o chat solicita um arquivo. O frontend deve enviar o arquivo junto com o ID da conversa e os dados pendentes que recebeu do endpoint /chat."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Processo finalizado com sucesso!"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dados ou arquivo ausentes/inválidos")
    })
    public ResponseEntity<ApiResponse<ChatResponseDTO>> handleChatUpload(
            @RequestPart("file")
            @Parameter(description = "O arquivo de documento enviado pelo usuário.")
            MultipartFile file,

            @RequestPart("conversationId")
            @Parameter(description = "O ID da conversa atual.")
            String conversationId,

            @RequestPart("pendingData")
            @Parameter(description = "A string JSON com os dados da solicitação coletados pelo chatbot, recebida na chamada anterior do /chat.")
            String pendingDataJson
    ) throws Exception {

        ApiResponse<ChatResponseDTO> response = chatService.processarUploadPendente(
                file, conversationId, pendingDataJson
        );
        return ResponseEntity.ok(response);
    }
}

