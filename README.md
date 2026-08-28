# API de votação cooperativa

Projeto desenvolvido para gerenciar pautas e votações de uma cooperativa. Cada associado pode votar uma vez em cada pauta.

## Tecnologias

- Java 17
- Spring Boot 3.3.5
- Maven
- MongoDB
- Docker e Docker Compose
- JUnit 5 e Mockito
- Swagger/OpenAPI

## Organização do projeto

O projeto foi separado em camadas simples:

```text
controller    endpoints da API
service       regras de negócio
repository    acesso ao MongoDB
model         documentos persistidos
dto           dados de entrada e saída da API
client        consulta de elegibilidade por CPF
exception     erros tratados pela API
```

Não foi utilizada uma arquitetura mais complexa porque, para o tamanho do desafio, essas camadas já separam bem as responsabilidades.

## Como executar

É necessário ter Java 17, Maven e MongoDB instalados.

```bash
mvn clean test
mvn spring-boot:run
```

Por padrão, a aplicação tenta se conectar em:

```text
mongodb://localhost:27017/voting
```

Para usar outra instância do MongoDB, defina a variável de ambiente `MONGODB_URI`.

## Executando com Docker

Com Docker Desktop no Windows/macOS ou Docker Engine no Linux:

```bash
docker compose up --build
```

O Compose inicia a API em `http://localhost:8080` e o MongoDB na porta `27017`.

Para parar os containers:

```bash
docker compose down
```

## Documentação da API

Com a aplicação em execução, o Swagger fica disponível em:

```text
http://localhost:8080/swagger-ui.html
```

## Endpoints

| Método | Endpoint | Descrição |
| --- | --- | --- |
| POST | `/api/v1/agendas` | Cria uma pauta |
| POST | `/api/v1/agendas/{agendaId}/sessions` | Abre uma sessão de votação |
| POST | `/api/v1/agendas/{agendaId}/votes` | Registra um voto |
| GET | `/api/v1/agendas/{agendaId}/result` | Consulta o resultado |

### Criar pauta

```http
POST /api/v1/agendas
Content-Type: application/json

{
  "title": "Orçamento anual",
  "description": "Votação do orçamento da cooperativa"
}
```

### Abrir sessão

```http
POST /api/v1/agendas/{agendaId}/sessions
Content-Type: application/json

{
  "durationMinutes": 5
}
```

Se a duração não for enviada, a sessão fica aberta por um minuto.

### Registrar voto

```http
POST /api/v1/agendas/{agendaId}/votes
Content-Type: application/json

{
  "associateId": "123456",
  "cpf": "12345678909",
  "vote": "YES"
}
```

Os votos aceitos são `YES` e `NO`.

### Consultar resultado

```http
GET /api/v1/agendas/{agendaId}/result
```

Exemplo de retorno:

```json
{
  "agendaId": "123",
  "yesVotes": 10,
  "noVotes": 4,
  "totalVotes": 14,
  "result": "APPROVED"
}
```

O resultado é `APPROVED`, `REJECTED` ou `TIED`.

## Regras principais

- Não é possível abrir sessão para uma pauta inexistente.
- Cada pauta possui uma única sessão de votação.
- Não é possível votar sem sessão ou após o horário de encerramento.
- Um associado vota somente uma vez por pauta.
- O empate retorna `TIED`.

## Votos duplicados e concorrência

O MongoDB possui um índice único composto por `agendaId` e `associateId`.

```text
agendaId + associateId
```

Essa regra é garantida pelo banco. Caso duas requisições tentem registrar o mesmo voto ao mesmo tempo, uma delas recebe erro de chave duplicada, tratado pela API como HTTP 409.

## Performance

O resultado não busca todos os votos na memória. A contagem é feita com consultas por pauta e tipo de voto.

Também foi incluído um teste simples de carga com K6 em `performance/voting-test.js`.

Depois de criar uma pauta e abrir a sessão, execute:

```bash
k6 run -e AGENDA_ID=ID_DA_PAUTA performance/voting-test.js
```

## Integração de CPF

Por padrão, a consulta externa fica desabilitada para facilitar a execução local:

```text
USER_INFO_ENABLED=false
```

Quando habilitada, a aplicação consulta:

```text
GET https://user-info.herokuapp.com/users/{cpf}
```

O status `ABLE_TO_VOTE` permite o voto. CPF inválido, associado sem permissão ou indisponibilidade do serviço retornam erro apropriado.

Para habilitar:

```bash
USER_INFO_ENABLED=true
```

Também é possível alterar a URL usando `USER_INFO_BASE_URL`.

## Erros

As respostas de erro seguem este formato:

```json
{
  "timestamp": "2026-08-28T15:00:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "O associado já votou nesta pauta",
  "path": "/api/v1/agendas/123/votes"
}
```

Os principais status usados são 400, 404, 409, 422 e 503.

## Versionamento

Foi utilizado versionamento na URL com `/api/v1`. É uma forma simples de deixar explícita a versão da API e permitir uma nova versão futuramente sem quebrar os consumidores atuais.

## Testes

Para executar os testes:

```bash
mvn clean test
```

Os testes unitários cobrem criação de pauta, abertura de sessão, duração padrão e personalizada, voto duplicado, sessão encerrada, empate e validação de CPF.

## Observação sobre o contrato mobile

O enunciado menciona telas `FORMULARIO` e `SELECAO`, mas o contrato JSON completo desse cliente não foi fornecido. Por isso, foram implementados apenas os endpoints REST do domínio. Caso esse contrato seja disponibilizado, a representação específica pode ser criada em DTOs separados.
