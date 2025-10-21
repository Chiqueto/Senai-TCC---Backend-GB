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
import com.senai.gestao_beneficios.service.medico.MedicoService;
import com.senai.gestao_beneficios.service.solicitacao.SolicitacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
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
            String idDependente,
            String descricao
    ) {}


    public ApiResponse<ChatResponseDTO> getChatResponse(ChatRequestDTO requestDTO) throws Exception {

        // --- 1. GERENCIA O HISTÓRICO E A MENSAGEM DO USUÁRIO ---
        String conversationId = (requestDTO.conversationId() == null || requestDTO.conversationId().isBlank())
                ? UUID.randomUUID().toString()
                : requestDTO.conversationId();

        List<Map<String, Object>> messages = historyService.getHistory(conversationId);
        if (messages.isEmpty()) {
            messages.add(Map.of("role", "system", "content", getSystemPrompt(FUSO_HORARIO_NEGOCIO)));
        }
        Map<String, Object> userMessage = Map.of("role", "user", "content", requestDTO.mensagem());
        messages.add(userMessage);

        // --- 2. FAZ A PRIMEIRA CHAMADA À IA ---
        ChatCompletionResponse initialResponse = makeApiCall(messages);
        ChatResponseMessage responseMessage = initialResponse.choices().getFirst().message();
        messages.add(objectMapper.convertValue(responseMessage, new TypeReference<>() {}));

        if (responseMessage.tool_calls() != null && !responseMessage.tool_calls().isEmpty()) {

            ToolCall toolCall = responseMessage.tool_calls().getFirst();

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String matricula = authentication.getName();
            Colaborador colaboradorLogado = colaboradorRepository.findByMatricula(matricula)
                    .orElseThrow(() -> new RuntimeException("Usuário autenticado não encontrado."));

            String toolResult = executeTool(toolCall.function().name(), toolCall.function().arguments(), colaboradorLogado);

            Map<String, Object> toolResponseMessage = Map.of("role", "tool", "tool_call_id", toolCall.id(), "content", toolResult);
            messages.add(toolResponseMessage);

            ChatCompletionResponse finalResponse = makeApiCall(messages);
            ChatResponseMessage finalResponseMessage = finalResponse.choices().getFirst().message();
            messages.add(objectMapper.convertValue(finalResponseMessage, new TypeReference<>() {}));

            historyService.saveHistory(conversationId, messages); // Supondo um método para salvar a lista

            return new ApiResponse<>(true, new ChatResponseDTO(finalResponseMessage.content(), conversationId), null, null, "Mensagem retornada com sucesso!");
        } else {
            historyService.saveHistory(conversationId, messages);
            return new ApiResponse<>(true, new ChatResponseDTO(responseMessage.content(), conversationId), null, null, "Mensagem retornada com sucesso!");
        }
    }

    private ChatCompletionResponse makeApiCall(List<Map<String, Object>> messages) {
        Map<String, Object> requestBody = Map.of(
                "model", "openai/gpt-4o",
                "messages", messages,
                "tools", getToolDefinitions()
        );

        try {
            System.out.println(">>> Tentando API principal (GitHub)...");

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
                        agendamentoArgs.idDependente(),
                        agendamentoArgs.horario()
                );

                ApiResponse<AgendamentoResponseDTO> agendamentoResponse = agendamentoService.criarAgendamento(agendamentoDTO);

                return objectMapper.writeValueAsString(agendamentoResponse.data());

            case "criar_solicitacao_beneficio":
                SolicitacaoArgs solicitacaoArgs = objectMapper.readValue(arguments, SolicitacaoArgs.class);

                SolicitacaoRequestDTO solicitacaoDTO = new SolicitacaoRequestDTO(
                        colaboradorLogado.getId(),
                        solicitacaoArgs.idBeneficio(),
                        new BigDecimal(solicitacaoArgs.valorTotal()),
                        solicitacaoArgs.idDependente(),
                        solicitacaoArgs.descricao(),
                        solicitacaoArgs.qtdeParcelas() != null ? Integer.parseInt(solicitacaoArgs.qtdeParcelas()) : null,
                        solicitacaoArgs.tipoPagamento()
                );

                // 3. Chama o service
                ApiResponse<SolicitacaoResponseDTO> solicitacaoResponse = solicitacaoService.criarSolicitacao(solicitacaoDTO);

                // 4. Retorna o resultado para a IA
                return objectMapper.writeValueAsString(solicitacaoResponse.data());

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
                                                "nomeDependente", Map.of("type", "string", "description", "O NOME do dependente, se a consulta não for para o próprio colaborador.")
                                        ),
                                        "required", List.of("idMedico", "horario")
                                )
                        )
                ),

                Map.of(
                        "type", "function",
                        "function", Map.of(
                                "name", "criar_solicitacao_beneficio",
                                "description", "Inicia uma nova solicitação de benefício para o colaborador logado.",
                                "parameters", Map.of(
                                        "type", "object",
                                        "properties", Map.of(
                                                "idBeneficio", Map.of("type", "string", "description", "O ID do benefício escolhido."),
                                                "valorTotal", Map.of("type", "string", "description", "O valor monetário total do benefício solicitado."),
                                                "tipoPagamento", Map.of(
                                                        "type", "string",
                                                        "description", "A forma de pagamento escolhida.",
                                                        "enum", List.of("DOACAO", "PAGAMENTO_PROPRIO", "DESCONTADO_EM_FOLHA")
                                                ),
                                                "qtdeParcelas", Map.of("type", "string", "description", "A quantidade de parcelas."),
                                                // --- PARÂMETRO ALTERADO ---
                                                "nomeDependente", Map.of("type", "string", "description", "O NOME do dependente, se o benefício for para um."),
                                                "descricao", Map.of("type", "string", "description", "Um texto com observações ou justificativas.")
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
                "4.  **`criar_agendamento(idColaborador: string, idMedico: string, horario: string, idDependente: string | null)`**:\n" +
                "    * **Descrição:** Cria um novo agendamento de consulta.\n" +
                "    * **Parâmetros:** `idColaborador` (obrigatório), `idMedico` (obrigatório), `horario` (obrigatório, a string UTC exata retornada por `listar_horarios_disponiveis`), `idDependente` (opcional).\n" +
                "    * **Quando usar:** Como passo final do fluxo de agendamento, após o colaborador confirmar o horário.\n" +
                "\n" +
                "5.  **`criar_solicitacao_beneficio(idColaborador: string, idBeneficio: string, valorTotal: number, tipoPagamento: string, qtdeParcelas: number | null, idDependente: string | null, descricao: string | null)`**:\n" +
                "    * **Descrição:** Inicia uma nova solicitação de benefício.\n" +
                "    * **Parâmetros:**\n" +
                "        * `idColaborador`: `string` (obrigatório). Use sempre o ID do usuário logado.\n" +
                "        * `idBeneficio`: `string` (obrigatório). O ID do benefício que o colaborador escolheu.\n" +
                "        * `valorTotal`: `number` (obrigatório). O valor monetário total do benefício solicitado.\n" +
                "        * `tipoPagamento`: `string` (obrigatório). Valores possíveis: `DOACAO`, `PAGAMENTO_PROPRIO`, `DESCONTADO_FOLHA`.\n" +
                "        * `qtdeParcelas`: `number` (obrigatório **apenas se** `tipoPagamento` for `DESCONTADO_FOLHA`). Para outros tipos, deve ser `null`.\n" +
                "        * `idDependente`: `string` (opcional). Use apenas se o benefício for para um dependente.\n" +
                "        * `descricao`: `string` (opcional). Campo de texto livre para o colaborador adicionar observações ou justificativas, quando o colaborador não informar, crie uma descrição genérica com as informações da solicitação.\n" +
                "    * **Quando usar:** Como passo final do fluxo de solicitação, após o colaborador ter escolhido o benefício e fornecido todos os detalhes necessários.\n" +
                "\n" +
                "### FLUXOS DE CONVERSA ESPERADOS ###\n" +
                "\n" +
                "**Fluxo 1: Agendamento de Consulta**\n" +
                "1.  Identifique a intenção de agendar.\n" +
                "2.  Pergunte pela especialidade desejada. Se o usuário não souber, use `listar_medicos_por_especialidade()` sem parâmetros para mostrar as opções.\n" +
                "3.  Após a escolha do médico, pergunte para qual dia ele deseja o agendamento, sempre diga em quais dias da semana ele atende (Domingo = 0, e assim por diante, essa é a lista de disponibilidade que vem com o médico).\n" +
                "4.  Use `listar_horarios_disponiveis(idMedico, dia)` para obter os slots.\n" +
                "5.  Apresente os horários disponíveis para o usuário de forma amigável (ex: \"Temos horários às 09:30, 10:00...\").\n" +
                "6.  Após a confirmação do usuário, pergunte se a consulta é para ele mesmo ou para um dependente.\n" +
                "7.  Use `criar_agendamento()` com todos os parâmetros coletados.\n" +
                "8.  Informe ao usuário se o agendamento foi confirmado com sucesso ou se houve algum erro.\n" +
                "\n" +
                "**Fluxo 2: Solicitação de Benefício**\n" +
                "1.  Identifique a intenção de solicitar um benefício.\n" +
                "2.  Pergunte qual benefício o colaborador deseja. Se ele não souber, use `listar_beneficios()` para mostrar as opções.\n" +
                "3.  Com base no benefício escolhido, determine quais informações são necessárias (se precisa de valor, se precisa de parcelas, etc.). Faça as perguntas necessárias ao colaborador.\n" +
                "4.  Use `criar_solicitacao_beneficio()` com todos os parâmetros coletados.\n" +
                "5.  Após a criação, informe ao colaborador os próximos passos do processo, como a necessidade de enviar um documento ou aguardar a aprovação da gestão.\n" +
                "\n" +
                "### REGRAS GERAIS ###\n" +
                "1.  **Sempre colete as informações passo a passo.** Não peça tudo de uma vez.\n" +
                "2.  **Nunca execute uma ação final sem a confirmação explícita do usuário.** (ex: \"Confirma o agendamento para terça (dia 20/11/2025) às 10:00 com o Dr. Carlos?\").\n" +
                "3.  **Use a identidade do usuário logado:** O `idColaborador` para as ferramentas deve ser sempre o do usuário que está interagindo com você. Não pergunte a ele qual é o seu ID.\n" +
                "4.  **Se não souber:** Se a pergunta do usuário fugir do escopo de agendamentos ou benefícios, ou se você não tiver uma ferramenta para ajudar, direcione-o para o canal oficial: \"Para este assunto, por favor, entre em contato diretamente com o RH.\n"+
                "5.  **AJA PRIMEIRO, FALE DEPOIS:** Se a mensagem do usuário for uma pergunta direta que pode ser respondida imediatamente por uma ferramenta sem parâmetros (como `listar_beneficios` ou `listar_medicos`), sua primeira ação deve ser chamar a ferramenta. Não responda com texto de confirmação como \"Vou buscar para você\". Chame a ferramenta, receba o resultado, e só então formule a resposta em texto para o usuário já contendo a informação solicitada.";
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