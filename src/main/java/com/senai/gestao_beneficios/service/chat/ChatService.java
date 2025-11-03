package com.senai.gestao_beneficios.service.chat;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.senai.gestao_beneficios.DTO.agendamento.AgendamentoRequestDTO;
import com.senai.gestao_beneficios.DTO.agendamento.AgendamentoResponseDTO;
import com.senai.gestao_beneficios.DTO.chat.ChatRequestDTO;
import com.senai.gestao_beneficios.DTO.chat.ChatResponseDTO;
import com.senai.gestao_beneficios.DTO.colaborador.ColaboradorDTO;
import com.senai.gestao_beneficios.DTO.dependente.DependenteDTO;
import com.senai.gestao_beneficios.DTO.medico.MedicoAvaiabilityDTO;
import com.senai.gestao_beneficios.DTO.reponsePattern.ApiResponse;
import com.senai.gestao_beneficios.DTO.solicitacao.SolicitacaoRequestDTO;
import com.senai.gestao_beneficios.DTO.solicitacao.SolicitacaoResponseDTO;
import com.senai.gestao_beneficios.domain.colaborador.Colaborador;
import com.senai.gestao_beneficios.domain.solicitacao.TipoPagamento;
import com.senai.gestao_beneficios.infra.exceptions.NotFoundException;
import com.senai.gestao_beneficios.repository.ColaboradorRepository;
import com.senai.gestao_beneficios.service.ColaboradorService;
import com.senai.gestao_beneficios.service.agendamento.AgendamentoService;
import com.senai.gestao_beneficios.service.beneficio.BeneficioService;
import com.senai.gestao_beneficios.service.documento.B2Service;
import com.senai.gestao_beneficios.service.medico.MedicoService;
import com.senai.gestao_beneficios.service.solicitacao.SolicitacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@RequiredArgsConstructor
public class ChatService {

    @Qualifier("githubWebClient")
    private final WebClient githubWebClient;

    @Qualifier("huggingFaceWebClient")
    private final WebClient huggingFaceWebClient;
    private final ObjectMapper objectMapper;

    private final BeneficioService beneficioService;
    private final MedicoService medicoService;
    private final AgendamentoService agendamentoService;
    private final SolicitacaoService solicitacaoService;
    private final ColaboradorService colaboradorService;
    private final ChatHistoryService historyService;
    private final ColaboradorRepository colaboradorRepository;
    private final B2Service documentoService;
    private static final ZoneId FUSO_HORARIO_NEGOCIO = ZoneId.of("America/Sao_Paulo");


    private record FunctionCall(String name, String arguments) {}
    private record ToolCall(String id, String type, FunctionCall function) {}
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record ChatResponseMessage(String role, String content, List<ToolCall> tool_calls) {}
    private record Choice(ChatResponseMessage message) {}
    private record ChatCompletionResponse(List<Choice> choices) {}

    private record HorariosArgs(String idMedico, LocalDate dia) {}
    private record AgendamentoArgs(String idMedico, Instant horario, String idDependente, String nomeDependente) {}
    private record SolicitacaoArgs(
            String idBeneficio,
            String valorTotal,
            TipoPagamento tipoPagamento,
            String qtdeParcelas,
            String nomeDependente,
            String descricao
    ) {}


    public ApiResponse<ChatResponseDTO> getChatResponse(ChatRequestDTO requestDTO) throws Exception {

        String conversationId = (requestDTO.conversationId() == null || requestDTO.conversationId().isBlank())
                ? UUID.randomUUID().toString() : requestDTO.conversationId();
        List<Map<String, Object>> messages = historyService.getHistory(conversationId);
        if (messages.isEmpty()) {
            messages.add(Map.of("role", "system", "content", getSystemPrompt(FUSO_HORARIO_NEGOCIO)));
        }
        messages.add(Map.of("role", "user", "content", requestDTO.mensagem()));

        ChatCompletionResponse initialResponse = makeApiCall(messages);
        ChatResponseMessage responseMessage = initialResponse.choices().getFirst().message();
        messages.add(objectMapper.convertValue(responseMessage, new TypeReference<>() {
        }));

        Colaborador colaboradorLogado = null;
        if (responseMessage.tool_calls() != null && !responseMessage.tool_calls().isEmpty()) {
            ToolCall toolCall = responseMessage.tool_calls().getFirst();
            String toolName = toolCall.function().name();
            String toolArgumentsJson = toolCall.function().arguments();
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String matricula = authentication.getName();
            colaboradorLogado = colaboradorRepository.findByMatricula(matricula)
                    .orElseThrow(() -> new RuntimeException("Usuário autenticado não encontrado."));

            if ("aguardar_documento_para_solicitacao".equals(toolName)) {

                // 1. Parseia os dados que a IA coletou. Isso vira o 'pendingData'.
                SolicitacaoArgs pendingData = objectMapper.readValue(toolArgumentsJson, SolicitacaoArgs.class);

                // 2. Define a mensagem de pausa (conforme seu SystemPrompt)
                String pauseMessage = "Entendido. Para prosseguir com a sua solicitação, por favor, anexe agora o documento de comprovação (com um orçamento).";

                // 3. Salva o histórico (importante para o próximo passo)
                historyService.saveHistory(conversationId, messages, colaboradorLogado);

                // 4. Cria a Resposta Especial com nextAction e pendingData
                // (Assumindo que seu DTO tem os campos: resposta, conversationId, nextAction, pendingData)
                ChatResponseDTO chatResponse = new ChatResponseDTO(
                        pauseMessage,
                        conversationId,
                        "AWAITING_UPLOAD", // <-- O nextAction que você quer
                        pendingData        // <-- Os dados para o /chat/upload
                );

                // 5. Retorna IMEDIATAMENTE para o frontend
                return new ApiResponse<>(true, chatResponse, null, null, "Aguardando documento para upload.");
            }

            if ("criar_agendamento".equals(toolName)) {
                Map<String, Object> argumentsMap = objectMapper.readValue(toolArgumentsJson, new TypeReference<>() {
                });

                if (argumentsMap.containsKey("nomeDependente") && argumentsMap.get("nomeDependente") != null) {
                    String nomeDependente = (String) argumentsMap.get("nomeDependente");

                    // Busca o ID do dependente pelo nome
                    String idDependente = findDependenteIdByName(colaboradorLogado.getId(), nomeDependente);

                    // Remove o 'nomeDependente' e adiciona o 'idDependente'
                    argumentsMap.remove("nomeDependente");
                    argumentsMap.put("idDependente", idDependente);

                    // Atualiza a string JSON com os argumentos corrigidos
                    toolArgumentsJson = objectMapper.writeValueAsString(argumentsMap);
                }
            }

            // --- FIM DA LÓGICA DE TRADUÇÃO ---

            // Agora, o executeTool sempre receberá o 'idDependente' se necessário
            String toolResult = executeTool(toolName, toolArgumentsJson, colaboradorLogado);

            Map<String, Object> toolResponseMessage = Map.of("role", "tool", "tool_call_id", toolCall.id(), "content", toolResult);
            messages.add(toolResponseMessage);

            ChatCompletionResponse finalResponse = makeApiCall(messages);
            ChatResponseMessage finalResponseMessage = finalResponse.choices().getFirst().message();
            messages.add(objectMapper.convertValue(finalResponseMessage, new TypeReference<>() {
            }));

            historyService.saveHistory(conversationId, messages, colaboradorLogado);

            return new ApiResponse<>(true, new ChatResponseDTO(finalResponseMessage.content(), conversationId), null, null, "Mensagem retornada com sucesso!");
        } else {
            historyService.saveHistory(conversationId, messages, colaboradorLogado);
            return new ApiResponse<>(true, new ChatResponseDTO(responseMessage.content(), conversationId), null, null, "Mensagem retornada com sucesso!");
        }
    }

    public ApiResponse<ChatResponseDTO> processarUploadPendente(
            MultipartFile file,
            String conversationId,
            String pendingDataJson
    ) throws Exception {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String matricula = authentication.getName();
        Colaborador colaboradorLogado = colaboradorRepository.findByMatricula(matricula)
                .orElseThrow(() -> new RuntimeException("Usuário autenticado não encontrado."));

        SolicitacaoArgs solicitacaoArgs = objectMapper.readValue(pendingDataJson, SolicitacaoArgs.class);
        SolicitacaoRequestDTO solicitacaoDTO = new SolicitacaoRequestDTO(
                colaboradorLogado.getId(),
                solicitacaoArgs.idBeneficio(),
                new BigDecimal(solicitacaoArgs.valorTotal()),
                solicitacaoArgs.nomeDependente != null ? findDependenteIdByName(colaboradorLogado.getId(), solicitacaoArgs.nomeDependente) : null,
                solicitacaoArgs.descricao(),
                solicitacaoArgs.qtdeParcelas() != null ? Integer.parseInt(solicitacaoArgs.qtdeParcelas()) : null,
                solicitacaoArgs.tipoPagamento()
        );

        ApiResponse<SolicitacaoResponseDTO> solicitacaoResponse = solicitacaoService.criarSolicitacao(solicitacaoDTO);
        String solicitacaoId = solicitacaoResponse.data().id();

        // 4. Envia o documento para a solicitação recém-criada
        documentoService.salvarArquivoNoB2(file, solicitacaoId, colaboradorLogado.getId());

        // 5. Formula uma resposta final de sucesso
        String respostaFinal = "Obrigado! Seu documento foi recebido e a solicitação de número " + solicitacaoId + " foi criada com sucesso. A gestão irá analisar e você será notificado.";

        // Salva o estado final da conversa
        List<Map<String, Object>> messages = historyService.getHistory(conversationId);
        messages.add(Map.of("role", "assistant", "content", respostaFinal));
        historyService.saveHistory(conversationId, messages, colaboradorLogado);

        return new ApiResponse<>(true, new ChatResponseDTO(respostaFinal, conversationId), null, null, "Processo finalizado.");
    }

    private ChatCompletionResponse makeApiCall(List<Map<String, Object>> messages) {
        Map<String, Object> requestBody = Map.of(
                "model", "openai/gpt-4o",
                "messages", messages,
                "tools", getToolDefinitions()
        );

        try {
            System.out.println(">>> Tentando API principal (GitHub)...");
            System.out.println(requestBody);
            return githubWebClient.post()
                    .uri("/chat/completions") // O URI pode ser diferente dependendo da sua baseUrl
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(ChatCompletionResponse.class)
                    .block();
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 429) {
                System.err.println("!!! Rate limit atingido na API principal. Acionando fallback para Hugging Face...");

                Map<String, Object> hfRequestBody = Map.of(
                        "model", "meta-llama/Llama-4-Scout-17B-16E-Instruct:groq", // Modelo do Hugging Face
                        "messages", messages,
                        "tools", getToolDefinitions()
                );

                System.out.println(hfRequestBody);

                return huggingFaceWebClient.post()
                        .uri("/chat/completions")
                        .bodyValue(hfRequestBody)
                        .retrieve()
                        .bodyToMono(ChatCompletionResponse.class)
                        .block();
            } else {
                throw e;
            }
        }
    }

    private String executeTool(String toolName, String arguments, Colaborador colaboradorLogado) throws Exception {
        // Este "roteador" chama o service correto com base no nome da ferramenta
        switch (toolName) {
            case "listar_beneficios":
                var beneficios = beneficioService.buscarBeneficios();
                return objectMapper.writeValueAsString(beneficios);

            case "listar_medicos":
                var medicos = medicoService.getMedicos();
                return objectMapper.writeValueAsString(medicos);

            case "listar_horarios_disponiveis":
                HorariosArgs args = objectMapper.readValue(arguments, HorariosArgs.class);
                ApiResponse<List<MedicoAvaiabilityDTO>> apiResponse = medicoService.buscarDisponibilidade(args.idMedico(), args.dia());
                System.out.println(apiResponse);
                return objectMapper.writeValueAsString(apiResponse.data());

            case "buscar_dependentes_do_colaborador":
                ApiResponse<ColaboradorDTO> response = colaboradorService.getUserById(colaboradorLogado.getId());
                return objectMapper.writeValueAsString(response.data().dependentes());

            case "criar_agendamento":
                AgendamentoArgs agendamentoArgs = objectMapper.readValue(arguments, AgendamentoArgs.class);

                String idDependenteAgendamento = null;
                if (agendamentoArgs.nomeDependente() != null && !agendamentoArgs.nomeDependente().isBlank()) {
                    // Usa a mesma lógica do 'case' acima para encontrar o ID do dependente pelo nome
                    idDependenteAgendamento = findDependenteIdByName(colaboradorLogado.getId(), agendamentoArgs.nomeDependente());
                }

                AgendamentoRequestDTO agendamentoDTO = new AgendamentoRequestDTO(
                        colaboradorLogado.getId(),
                        agendamentoArgs.idMedico(),
                        idDependenteAgendamento,
                        agendamentoArgs.horario()
                );

                ApiResponse<AgendamentoResponseDTO> agendamentoResponse = agendamentoService.criarAgendamento(agendamentoDTO);

                return objectMapper.writeValueAsString(agendamentoResponse.data());

            default:
                return "{\"error\": \"Ferramenta desconhecida: " + toolName + "\"}";
        }
    }

    private List<Map<String, Object>> getToolDefinitions() {
        return List.of(
                // Ferramenta 1: listar_beneficios (Correta, sem parâmetros)
                Map.of(
                        "type", "function",
                        "function", Map.of(
                                "name", "listar_beneficios",
                                "description", "Retorna uma lista de todos os benefícios disponíveis para solicitação.",
                                "parameters", Map.of("type", "object", "properties", Map.of())
                        )
                ),

                // Ferramenta 2: listar_medicos (Correta, sem parâmetros)
                Map.of(
                        "type", "function",
                        "function", Map.of(
                                "name", "listar_medicos",
                                "description", "Retorna uma lista de médicos disponíveis e suas respectivas especialidades.",
                                "parameters", Map.of("type", "object", "properties", Map.of())
                        )
                ),

                // Ferramenta 3: listar_horarios_disponiveis (CORRIGIDA COM PARÂMETROS)
                Map.of(
                        "type", "function",
                        "function", Map.of(
                                "name", "listar_horarios_disponiveis",
                                "description", "Retorna a lista de horários disponíveis de um médico em um dia específico.",
                                "parameters", Map.of(
                                        "type", "object",
                                        "properties", Map.of(
                                                "idMedico", Map.of(
                                                        "type", "string",
                                                        "description", "O ID do médico para o qual buscar a disponibilidade."
                                                ),
                                                "dia", Map.of(
                                                        "type", "string",
                                                        "description", "O dia para verificar a disponibilidade, no formato AAAA-MM-DD."
                                                )
                                        ),
                                        "required", List.of("idMedico", "dia") // Informa à IA que ambos são obrigatórios
                                )
                        )
                ),
                Map.of(
                        "type", "function",
                        "function", Map.of(
                                "name", "buscar_dependentes_do_colaborador",
                                "description", "Retorna a lista de dependentes (com nome e ID) associados ao colaborador que está conversando.",
                                "parameters", Map.of("type", "object", "properties", Map.of()) // Não precisa de parâmetros
                        )
                ),
                // Ferramenta 4: criar_agendamento (NOVA)
                Map.of(
                        "type", "function",
                        "function", Map.of(
                                "name", "criar_agendamento",
                                "description", "Cria um novo agendamento de consulta para o colaborador logado.",
                                "parameters", Map.of(
                                        "type", "object",
                                        "properties", Map.of(
                                                "idMedico", Map.of("type", "string", "description", "O ID do médico escolhido."),
                                                "horario", Map.of("type", "string", "description", "O horário exato escolhido, em formato UTC ISO 8601."),
                                                // --- PARÂMETRO ALTERADO ---
                                                "nomeDependente", Map.of("type", List.of("string", "null"), "description", "O NOME do dependente, se a consulta não for para o próprio colaborador.")
                                        ),
                                        "required", List.of("idMedico", "horario")
                                )
                        )
                ),

                Map.of(
                        "type", "function",
                        "function", Map.of(
                                "name", "aguardar_documento_para_solicitacao",
                                "description", "Deve ser usada QUANDO todos os dados de uma solicitação de benefício forem coletados e o próximo passo for pedir o documento ao usuário.",
                                "parameters", Map.of(
                                        "type", "object",
                                        "properties", Map.of(
                                                "idBeneficio", Map.of("type", "string"),
                                                "valorTotal", Map.of("type", "string"),
                                                "tipoPagamento", Map.of("type", "string", "enum", List.of("DOACAO", "PAGAMENTO_PROPRIO", "DESCONTADO_FOLHA")),
                                                "qtdeParcelas", Map.of("type", List.of("string", "null")),
                                                "nomeDependente", Map.of("type", List.of("string", "null")),
                                                "descricao", Map.of("type", List.of("string", "null"))
                                        ),
                                        "required", List.of("idBeneficio", "valorTotal", "tipoPagamento")
                                )
                        )
                )
        );
    }

    private String getSystemPrompt(ZoneId zoneId) {
        String dataHoraAtual = ZonedDateTime.now(zoneId)
                .format(DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy, HH:mm (zzzz)"));

        return "A data e hora atuais são: " + dataHoraAtual + ". Use esta informação como referência para todas as perguntas relacionadas a tempo (como \"hoje\", \"amanhã\", \"semana que vem\")." +
                "### PERSONA E DIRETIVA PRINCIPAL ###\n" +
                "Você é Oirem Ture A Mai, um assistente virtual especialista da área de Gestão de Benefícios da Usina Alta Mogiana. Seu tom é profissional, prestativo e claro. Sua função principal não é responder perguntas diretamente, mas sim guiar o colaborador em uma conversa natural para coletar as informações necessárias e, em seguida, utilizar as ferramentas (`tools`) disponíveis para executar ações no sistema, como agendar consultas ou solicitar benefícios. Você sempre se apresenta no início da conversa.\n" +
                "\n" +
                "### FERRAMENTAS DISPONÍVEIS (TOOLS) ###\n" +
                "Você tem acesso às seguintes ferramentas para interagir com o sistema. Você deve decidir qual ferramenta usar com base na intenção do usuário.\n" +
                "\n" +
                "1.  **`listar_beneficios()`**:\n" +
                "    * **Descrição:** Retorna uma lista de todos os benefícios disponíveis para solicitação.\n" +
                "    * **Parâmetros:** Nenhum.\n" +
                "    * **Quando usar:** Use quando o colaborador perguntar \"quais benefícios eu posso pedir?\" ou \"quais as opções disponíveis?\".\n" +
                "\n" +
                "2.  **`listar_medicos()`**:\n" +
                "    * **Descrição:** Retorna uma lista de médicos disponíveis e suas respectivas especialidades.\n" +
                "    * **Parâmetros:** Nenhum.\n" +
                "    * **Quando usar:** Use quando o colaborador pedir para ver os médicos ou procurar por um tipo específico (ex: \"tem cardiologista?\").\n" +
                "\n" +
                "3.  **`listar_horarios_disponiveis(idMedico: string, dia: string)`**:\n" +
                "    * **Descrição:** Retorna os horários disponíveis (em formato UTC ISO 8601) para um médico específico em um dia específico.\n" +
                "    * **Parâmetros:** `idMedico` (obrigatório), `dia` (obrigatório, formato AAAA-MM-DD).\n" +
                "    * **Quando usar:** Depois que o colaborador escolher um médico e um dia.\n" +
                "\n" +
                "4.  **`criar_agendamento(idMedico: string, horario: string, nomeDependente: string | null)`**:\n" +
                "    * **Descrição:** Cria um novo agendamento de consulta.\n" +
                "    * **Parâmetros:** `idMedico` (obrigatório), `horario` (obrigatório), `nomeDependente` (opcional - envie o NOME do dependente, ou null se for para o próprio colaborador).\n" +
                "    * **Quando usar:** Como passo final do fluxo de agendamento, após o colaborador confirmar o horário.\n" +
                "\n" +
                "5. **`buscar_dependentes_do_colaborador()`**:\n" +
                "    * **Descrição:** Retorna a lista de dependentes (com nome e ID) do colaborador logado. Use esta ferramenta para descobrir o ID de um dependente quando o usuário fornecer o nome.\n" +
                "\n" +
                "6.  **`aguardar_documento_para_solicitacao(idBeneficio: string, valorTotal: string, tipoPagamento: string, qtdeParcelas: string | null, nomeDependente: string | null, descricao: string | null)`**:\n" +
                "    * **Descrição:** Deve ser usada como passo final da coleta de dados para uma solicitação de benefício. Esta ferramenta sinaliza para o frontend que o usuário precisa enviar um documento para prosseguir.\n" +
                "    * **Quando usar:** Use QUANDO tiver todos os dados de uma solicitação e o próximo passo for pedir o documento ao usuário.\n" +
                "\n" +
                "### FLUXOS DE CONVERSA ESPERADOS ###\n" +
                "\n" +
                "**Fluxo 1: Agendamento de Consulta**\n" +
                "1.  Identifique a intenção de agendar.\n" +
                "2.  Pergunte pela especialidade desejada. Se o usuário não souber, use `listar_medicos_por_especialidade()` sem parâmetros para mostrar as opções.\n" +
                "3.  Após a escolha do médico, pergunte para qual dia ele deseja o agendamento, sempre diga em quais dias da semana ele atende (Domingo = 0, e assim por diante, essa é a lista de disponibilidade que vem com o médico).\n" +
                "4.  Caso você verifique que o dia da semana informado pelo usuário o médico não atende, já avise ele antes de usar `listar_horarios_disponíveis()`. Caso você já identifique que o médico atende no dia da semana informado, use `listar_horarios_disponiveis(idMedico, dia)` para obter os slots.\n" +
                "5.  Apresente os horários disponíveis para o usuário de forma amigável (ex: \"Temos horários às 09:30, 10:00...\").\n" +
                "6.  Após a confirmação do usuário, pergunte se a consulta é para ele mesmo ou para um dependente.\n" +
                "7.  Use `criar_agendamento()` com todos os parâmetros coletados.\n" +
                "8.  Informe ao usuário se o agendamento foi confirmado com sucesso ou se houve algum erro.\n" +
                "\n" +
                "**Fluxo 2: Solicitação de Benefício**\n" +
                "1.  Identifique a intenção de solicitar um benefício.\n" +
                "2.  Pergunte qual benefício ele deseja. Se ele não souber, use a ferramenta `listar_beneficios()` para mostrar as opções.\n" +
                "3.  Após o usuário escolher um benefício, colete as informações necessárias **uma por uma**:\n" +
                "    - \"Qual o valor total que você precisa?\"\n" +
                "    - \"Qual será a forma de pagamento?\"\n" +
                "    - Se a forma for `DESCONTADO_EM_FOLHA`, pergunte: \"Em quantas parcelas?\ caso contrário, sempre será 1 parcela \n" +
                "    - \"O benefício é para um dependente?\" Se sim, pergunte o nome.\n" +
                "    - \"Gere uma observação genérica\"\n" +
                "4.  Quando tiver coletado **todos** os dados, **NÃO confirme o sucesso**. Em vez disso, chame a ferramenta `aguardar_documento_para_solicitacao`, preenchendo todos os argumentos com os dados que você coletou.\n" + // (Vírgula extra removida daqui)
                "5.  Sua resposta final para o usuário deve ser **apenas** um texto simples pedindo o documento, como: \"Entendido. Para prosseguir com a sua solicitação, por favor, anexe agora o documento de comprovação (com um orçamento).\"\n" +
                "\n" +
                "### REGRAS GERAIS ###\n" +
                "1.  **Sempre colete as informações passo a passo.** Não peça tudo de uma vez.\n" +
                "2.  **NUNCA execute uma ação final sem a confirmação explícita do usuário.** (ex: \"Confirma o agendamento para terça (dia 20/11/2025) às 10:00 com o Dr. Carlos?\").\n" +
                "3.  **Use a identidade do usuário logado:** O `idColaborador` para as ferramentas deve ser sempre o do usuário que está interagindo com você. Não pergunte a ele qual é o seu ID.\n" +
                "4.  **Se não souber:** Se a pergunta do usuário fugir do escopo de agendamentos ou benefícios, ou se você não tiver uma ferramenta para ajudar, direcione-o para o canal oficial: \"Para este assunto, por favor, entre em contato diretamente com o RH.\n"+
                "5.  **AJA PRIMEIRO, FALE DEPOIS:** Se a mensagem do usuário for uma pergunta direta que pode ser respondida imediatamente por uma ferramenta sem parâmetros (como `listar_beneficios` ou `listar_medicos`), sua primeira ação deve ser chamar a ferramenta. Não responda com texto de confirmação como \"Vou buscar para você\". Chame a ferramenta, receba o resultado, e só então formule a resposta em texto para o usuário já contendo a informação solicitada.\n"+
                "6.  **REGRA DE OURO - CONFIRMAÇÃO PÓS-AÇÃO:** Você **NUNCA** deve informar ao usuário que uma ação (como criar um agendamento ou solicitação) foi concluída com sucesso ANTES de você usar a ferramenta correspondente (`criar_agendamento`, `aguardar_documento_para_solicitacao`) e receber de volta o resultado da execução dessa ferramenta. Sua resposta de sucesso **DEVE** ser baseada no retorno da ferramenta. Se a ferramenta retornar um erro, você deve informar o erro ao usuário.";
    }

    private String findDependenteIdByName(String colaboradorId, String nomeDependente) throws JsonProcessingException {
        ApiResponse<ColaboradorDTO> response = colaboradorService.getUserById(colaboradorId);
        return response.data().dependentes().stream()
                .filter(d -> d.nome().equalsIgnoreCase(nomeDependente))
                .findFirst()
                .map(DependenteDTO::id)
                .orElseThrow(() -> new NotFoundException("dependente", "Dependente com o nome '" + nomeDependente + "' não encontrado."));
    }

}
