# Gestão de Benefícios - Backend com Agente de IA

![Java](https://img.shields.io/badge/Java-21-blue.svg?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.x-green.svg?style=for-the-badge&logo=spring)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg?style=for-the-badge&logo=postgresql)
![JWT](https://img.shields.io/badge/JWT-Auth-purple.svg?style=for-the-badge&logo=jsonwebtokens)

> **Status do Projeto:** Em Desenvolvimento 🚀

Backend de um sistema corporativo para gestão de benefícios e agendamentos, potencializado por um agente de IA conversacional para automação de processos.

![Dashboard do Projeto](<img width="1905" height="858" alt="image" src="https://github.com/user-attachments/assets/dcee9d1b-2139-466d-90e5-dc141b6ec12e" />
)

## 📄 Sobre o Projeto

O **Gestão de Benefícios** é uma aplicação robusta desenvolvida em Java com Spring Boot, projetada para modernizar e automatizar os processos de RH de uma empresa. O sistema possui dois módulos principais: um para o gerenciamento completo do ciclo de vida de solicitações de benefícios (desde o pedido do colaborador até a aprovação da gestão) e outro para o agendamento de consultas médicas com profissionais da empresa.

O grande diferencial do projeto é a integração com um **agente de IA inteligente**, que permite aos usuários interagir com o sistema através de uma interface de chat em linguagem natural, automatizando tarefas que antes exigiriam navegação manual por múltiplas telas.

## ✨ Principais Funcionalidades

### 🤖 **Agente de IA Conversacional ("Oirem Ture A Mai")**
-   **Interface de Linguagem Natural:** Permite que colaboradores solicitem benefícios e agendem consultas através de uma conversa fluida.
-   **Function Calling:** O agente é capaz de entender a intenção do usuário e utilizar "ferramentas" que se conectam diretamente à API do backend para executar ações (listar médicos, verificar horários, criar agendamentos, etc.).
-   **Gerenciamento de Contexto:** O histórico da conversa é persistido no banco de dados, permitindo que o agente mantenha o contexto em múltiplos turnos de diálogo.
-   **Arquitetura Resiliente:** Implementa uma estratégia de **Retry com Backoff Exponencial** para lidar com limites de taxa (`429 Too Many Requests`) e um sistema de **Fallback** que alterna entre a API do GitHub Models e a do Hugging Face para garantir alta disponibilidade.

### 📑 **Gestão de Documentos e Aprovações**
-   **Fluxo de Aprovação Multi-etapas:** As solicitações passam por diferentes status (`PENDENTE`, `AGUARDANDO_ASSINATURA`, `APROVADO`), garantindo um controle rigoroso do processo.
-   **Geração Automática de PDFs:** O sistema gera documentos formais (`Autorização` e `Recibo de Ciência`) automaticamente após a aprovação da gestão, utilizando **Thymeleaf** para templating e **Flying Saucer** para a renderização.
-   **Customização Visual:** Os PDFs incluem elementos dinâmicos como fontes customizadas (Dancing Script), imagens (logo, carimbo) e dados preenchidos em tempo real.
-   **Armazenamento Seguro em Nuvem:** Todos os documentos são enviados para um bucket privado no **Backblaze B2** (compatível com API S3).
-   **Controle de Acesso:** O acesso a documentos sensíveis é feito exclusivamente através de **URLs pré-assinadas** com tempo de expiração, garantindo a segurança dos dados.

### 🗓️ **Sistema de Agendamentos e Análise de Dados**
-   **Lógica de Disponibilidade Complexa:** O sistema calcula os horários disponíveis de um médico em tempo real, considerando seu expediente, horários de pausa e agendamentos já existentes.
-   **Gerenciamento de Fuso Horário:** Todas as datas e horas são armazenadas em **UTC** (`java.time.Instant`) e tratadas corretamente na "borda" da aplicação, garantindo consistência.
-   **Tarefas Agendadas (`Scheduled Task`):** Um job automático roda diariamente para atualizar o status de agendamentos passados de `AGENDADO` para `CONCLUIDO`.
-   **Dashboard Analítico:** Um endpoint de agregação (`/dashboard/resumo`) fornece dados consolidados (KPIs e dados para gráficos) de forma eficiente, utilizando queries customizadas com `GROUP BY` e `COUNT`.

## 🛠️ Tecnologias Utilizadas

-   **Backend:** Java 21, Spring Boot (Web, Data JPA, Security)
-   **Banco de Dados:** PostgreSQL
-   **Persistência:** Hibernate, Spring Data JPA
-   **Segurança:** Spring Security, JWT (JSON Web Tokens)
-   **APIs de IA:** GitHub Models (via Azure AI SDK), Hugging Face Router
-   **Cliente HTTP:** Spring WebClient (reativo)
-   **Armazenamento em Nuvem:** Backblaze B2 (com AWS S3 SDK)
-   **Geração de PDF:** Thymeleaf, Flying Saucer (xhtmlrenderer)
-   **Documentação da API:** Swagger / OpenAPI
-   **Build:** Maven

## ⚙️ Como Executar o Projeto

### Pré-requisitos
-   JDK 21 ou superior
-   Maven 3.8+
-   PostgreSQL
-   Docker (opcional, para rodar o banco de dados)

### Configuração
1.  Clone o repositório:
    ```bash
    git clone https://SEU_LINK_DO_REPOSITORIO.git
    cd gestao_beneficios
    ```
2.  Crie um banco de dados PostgreSQL.
3.  Configure as variáveis de ambiente. Você pode criar um arquivo `.env` na raiz do projeto (se estiver usando `spring-dotenv`) ou configurar diretamente na sua IDE (Run/Debug Configurations).

    **Exemplo de variáveis necessárias:**
    ```
    DATABASE_URL=jdbc:postgresql://localhost:5432/seu_banco
    DATABASE_USERNAME=seu_usuario
    DATABASE_PASSWORD=sua_senha

    JWT_SECRET=sua-chave-secreta-super-longa-e-segura-aqui
    JWT_EXPIRATION=3600000

    B2_ACCESS_KEY=sua_chave_de_acesso_b2
    B2_SECRET_KEY=sua_chave_secreta_b2

    AZURE_AI_KEY=sua_chave_github_pat
    HF_TOKEN=sua_chave_hugging_face
    ```
4.  Certifique-se de ter os arquivos de fontes e imagens nas pastas corretas em `src/main/resources`.

### Execução
Execute o seguinte comando na raiz do projeto:
```bash
./mvnw spring-boot:run
```
A aplicação estará disponível em `http://localhost:8080`.

## 📚 Documentação da API

A documentação completa dos endpoints está disponível via Swagger UI. Após iniciar a aplicação, acesse:
[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

## 🧑‍💻 Autor

**[Luís Felipe Mozer Chiqueto]**

-   LinkedIn: [[Acesse aqui!](https://www.linkedin.com/in/luis-felipe-chiqueto/)]
-   GitHub: [[Meu github](https://github.com/Chiqueto)]
-   Portfólio: [[Acesse meu portifólio!](https://new-portifolio-smoky.vercel.app/)]

---
