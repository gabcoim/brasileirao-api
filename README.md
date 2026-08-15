# Brasileirão

Aplicação full stack para importar partidas da API-Football, armazená-las no
PostgreSQL e consultar temporadas, rodadas, classificação e desempenho dos
clubes.

O projeto foi desenvolvido para praticar arquitetura em camadas, integração com
API externa, persistência de dados e construção de uma API REST com Spring Boot.

## Funcionalidades

- Importação de partidas da API-Football.
- Criação e atualização de partidas sem duplicidade.
- Persistência de times e partidas no PostgreSQL.
- Consulta de partidas por liga e temporada.
- Consulta de partidas por rodada.
- Listagem das temporadas importadas.
- Classificação calculada a partir das partidas finalizadas.
- Página individual de cada clube com campanha e jogos disputados.
- Frontend responsivo com seleção de temporada e rodada.
- Testes de persistência utilizando H2.

## Tecnologias

- Java 21
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA
- Hibernate
- PostgreSQL
- H2 para testes
- Gradle
- HTML, CSS e JavaScript
- API-Football

## Arquitetura

O backend está dividido em camadas:

```text
Controller
    ↓ recebe a requisição HTTP
Service
    ↓ aplica as regras da aplicação
Repository
    ↓ consulta ou altera os dados
PostgreSQL
```

Para a importação, o fluxo também utiliza um client externo:

```text
ImportacaoController
    → PartidaImportacaoService
        → ApiFootballClient
            → API-Football
        → PartidaPersistenciaService
            → Repositories
                → PostgreSQL
```

## Demonstração rápida

O perfil `demo` permite conhecer o projeto sem instalar PostgreSQL, criar banco
ou possuir uma chave da API-Football. Ele inicia um H2 temporário e carrega um
campeonato simulado com 20 clubes fictícios, 38 rodadas e 380 partidas.

No Windows:

```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=demo"
```

No Linux ou macOS:

```bash
./gradlew bootRun --args="--spring.profiles.active=demo"
```

Depois, acesse:

```text
http://localhost:8080/
```

Nesse perfil, o endpoint de importação e o acesso à API-Football ficam
desativados. Os dados são demonstrativos e são recriados sempre que a aplicação
é iniciada.

### Publicando a demonstração

O repositório inclui um `Dockerfile` e um `render.yaml`. Depois de enviar o
projeto para o GitHub, ele pode ser conectado ao Render como um Blueprint. A
plataforma utiliza o perfil `demo`, portanto nenhuma senha de PostgreSQL ou chave
da API-Football precisa ser cadastrada para essa demonstração.

O endpoint `/actuator/health` é usado pelo Render para verificar se a aplicação
iniciou corretamente. No plano gratuito, o primeiro acesso depois de um período
sem visitantes pode levar cerca de um minuto.

## Publicação com dados reais

O `render.yaml` está configurado para o perfil `prod`. Nesse perfil, a aplicação
usa um PostgreSQL hospedado e mantém a importação administrativa ativa. Cadastre
as variáveis abaixo no serviço do Render:

| Variável | Conteúdo |
|---|---|
| `SPRING_DATASOURCE_URL` | URL JDBC do PostgreSQL, começando com `jdbc:postgresql://` |
| `SPRING_DATASOURCE_USERNAME` | Usuário do PostgreSQL hospedado |
| `SPRING_DATASOURCE_PASSWORD` | Senha do PostgreSQL hospedado |
| `API_FOOTBALL_KEY` | Chave da API-Football |
| `ADMIN_USERNAME` | Usuário administrativo, por padrão `admin` |
| `ADMIN_PASSWORD` | Senha forte para o endpoint de importação |

Exemplo do formato da URL JDBC para um banco Neon:

```text
jdbc:postgresql://HOST/neondb?sslmode=require
```

O Neon apresenta uma conexão semelhante a
`postgresql://USUARIO:SENHA@HOST/neondb`. Para a aplicação Java, separe o
usuário e a senha nas variáveis correspondentes e transforme o início da URL em
`jdbc:postgresql://`.

Depois que o deploy estiver saudável, faça a primeira importação usando HTTPS:

```powershell
$credencial = Get-Credential

Invoke-RestMethod `
  -Method Post `
  -Credential $credencial `
  -Uri "https://SEU-SERVICO.onrender.com/importacoes/partidas?ligaId=71&temporada=2024"
```

Repita somente para as temporadas que deseja disponibilizar. Os visitantes
podem consultar os endpoints `GET`, mas apenas o administrador pode importar ou
atualizar partidas.

## Pré-requisitos

Os pré-requisitos abaixo são necessários apenas para o modo normal, que importa
e persiste dados reais:

- Java 21
- PostgreSQL em execução na porta `5432`
- Banco chamado `postgres`
- Usuário do banco chamado `postgres`
- Chave da [API-Football](https://www.api-football.com/)

O plano gratuito da API pode limitar as temporadas disponíveis. No momento em
que o projeto foi desenvolvido, as temporadas de 2022 a 2024 estavam
disponíveis no plano gratuito.

## Variáveis de ambiente

O arquivo `.env.example` documenta os nomes necessários, mas este projeto não
carrega arquivos `.env` automaticamente. Defina as variáveis no sistema, no
terminal ou na configuração de execução da IDE.

No PowerShell:

```powershell
$env:API_FOOTBALL_KEY="sua-chave-da-api"
$env:POSTGRES_PASSWORD="sua-senha-do-postgres"
$env:ADMIN_USERNAME="admin"
$env:ADMIN_PASSWORD="uma-senha-administrativa-forte"
```

No IntelliJ, adicione as mesmas variáveis em:

```text
Run → Edit Configurations → Environment variables
```

## Executando a aplicação

No Windows:

```powershell
.\gradlew.bat bootRun
```

No Linux ou macOS:

```bash
./gradlew bootRun
```

Depois, acesse:

```text
http://localhost:8080/
```

## Importando partidas

O endpoint de importação é administrativo e exige HTTP Basic. Primeiro crie um
objeto de credencial com o mesmo usuário e senha definidos nas variáveis de
ambiente:

```powershell
$credencial = Get-Credential
```

Exemplo com o Brasileirão Série A, cujo ID na API-Football é `71`:

```powershell
Invoke-RestMethod `
  -Method Post `
  -Credential $credencial `
  -Uri "http://localhost:8080/importacoes/partidas?ligaId=71&temporada=2024"
```

Em uma aplicação publicada, envie essas credenciais somente através de HTTPS.

Resposta esperada:

```json
{
  "recebidas": 380,
  "criadas": 380,
  "atualizadas": 0,
  "ignoradas": 0
}
```

Uma nova importação da mesma temporada atualiza os registros existentes pelo ID
externo, em vez de duplicá-los.

## Endpoints

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/partidas?ligaId=71&temporada=2024` | Lista as partidas da temporada |
| `GET` | `/partidas/temporadas?ligaId=71` | Lista as temporadas importadas |
| `GET` | `/partidas/temporada/2024/rodada/10?ligaId=71` | Lista as partidas de uma rodada |
| `GET` | `/times` | Lista os times armazenados |
| `POST` | `/importacoes/partidas?ligaId=71&temporada=2024` | Importa ou atualiza partidas |

Os endpoints `GET` e o frontend são públicos. O endpoint `POST` de importação
exige um usuário com a função administrativa.

## Executando os testes

No Windows:

```powershell
.\gradlew.bat clean test
```

No Linux ou macOS:

```bash
./gradlew clean test
```

Os testes utilizam um banco H2 temporário e não modificam o PostgreSQL local.

## Estrutura principal

```text
src/main/java/com/meuprojeto/brasileirao
├── client       # Comunicação com a API-Football
├── config       # Configurações da aplicação
├── controller   # Endpoints HTTP
├── dto          # Formatos de entrada e saída
├── model        # Entidades do banco
├── repository   # Acesso ao PostgreSQL
└── service      # Regras e fluxos da aplicação
```

## Melhorias planejadas

- Tratamento global de erros.
- Documentação OpenAPI/Swagger.
- Migrações de banco com Flyway.
- Testes dos controllers.
- Docker Compose para aplicação e PostgreSQL.
- Publicação da demonstração em ambiente externo.

## Autor

Desenvolvido por gabcoim.
www.linkedin.com/in/gabriel-p-coimbra
https://github.com/gabcoim
