# =========================================================================
# ESTÁGIO 1: O "BUILDER" - Compila a aplicação
# Usa uma imagem com o JDK completo e ferramentas de build.
# =========================================================================
FROM eclipse-temurin:21-jdk-jammy as builder

# Define o diretório de trabalho dentro do container
WORKDIR /app

# Copia primeiro os arquivos de definição de dependências e o wrapper do Maven
# Isso otimiza o cache do Docker. As dependências só serão baixadas novamente se o pom.xml mudar.
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# =====> ADICIONE ESTA LINHA PARA DAR PERMISSÃO DE EXECUÇÃO <=====
RUN chmod +x ./mvnw

# Baixa todas as dependências do projeto
RUN ./mvnw dependency:go-offline

# Copia o resto do código-fonte da sua aplicação
COPY src ./src

# Compila a aplicação e gera o arquivo .jar. Pula os testes para agilizar o build.
RUN ./mvnw package -DskipTests


# =========================================================================
# ESTÁGIO 2: A IMAGEM FINAL - Otimizada para produção
# Usa uma imagem mínima, apenas com o Java Runtime (JRE).
# =========================================================================
FROM eclipse-temurin:21-jre-jammy

# Define o diretório de trabalho
WORKDIR /app

# Argumento para pegar o nome do JAR gerado no estágio de build
ARG JAR_FILE=target/*.jar

# Copia o arquivo .jar que foi gerado no estágio "builder" para a imagem final
COPY --from=builder /app/${JAR_FILE} app.jar

# Expõe a porta 8080 (o Render vai sobrescrever isso com a variável $PORT, o que é perfeito)
EXPOSE 8080

# Comando para iniciar a aplicação quando o container for executado
ENTRYPOINT ["java", "-jar", "app.jar"]