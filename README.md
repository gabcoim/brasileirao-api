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

``text
Controller
    ↓ recebe a requisição HTTP
Service
    ↓ aplica as regras da aplicação
Repository
    ↓ consulta ou altera os dados
PostgreSQL
``

Para a importação, o fluxo também utiliza um client externo:

``text
ImportacaoController
    → PartidaImportacaoService
        → ApiFootballClient
            → API-Football
        → PartidaPersistenciaService
            → Repositories
                → PostgreSQL
``
``

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
