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
## Como executar

Para executar sem Docker, é necessário ter Java 17, Maven e MongoDB instalados.

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

Com a validação externa de CPF desativada, crie uma pauta nova, abra uma sessão e execute:

```bash
k6 run -e AGENDA_ID=ID_DA_PAUTA performance/voting-test.js
```

No Windows, caso o K6 não esteja instalado:

```powershell
winget install k6 --source winget
```

O script faz 200 votos com até 20 usuários virtuais simultâneos. Use uma pauta nova a cada execução para não receber votos duplicados de testes anteriores.

## Integração de CPF

Por padrão, a consulta externa fica desabilitada para facilitar a execução local e os testes:

```text
USER_INFO_ENABLED=false
```

Quando habilitada, a aplicação consulta:

```text
GET https://user-info.herokuapp.com/users/{cpf}
```

O status `ABLE_TO_VOTE` permite o voto. CPF inválido retorna `404`, associado sem permissão retorna `422` e indisponibilidade do serviço retorna `503`.

Durante o desenvolvimento deste projeto, a URL originalmente fornecida pelo desafio retornou a página `No such app` da Heroku. Por isso, a integração permanece desativada por padrão. Caso seja fornecida uma URL funcional, é possível habilitá-la e configurá-la.

Com Docker no PowerShell:

```powershell
$env:USER_INFO_ENABLED = "true"
$env:USER_INFO_BASE_URL = "https://url-do-servico"
docker compose up -d --force-recreate
```

Sem Docker, defina as mesmas variáveis de ambiente antes de executar `mvn spring-boot:run`.

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

## Telas para o aplicativo mobile

O Anexo 1 define telas JSON interpretadas pelo aplicativo mobile. Foram adicionadas telas do tipo `FORMULARIO` para as operações que precisam de dados do usuário:

| Endpoint | Tela retornada |
| --- | --- |
| GET `/api/v1/mobile/agendas/formulario` | Cadastro de pauta |
| GET `/api/v1/mobile/agendas/{agendaId}/sessao` | Abertura de sessão |
| GET `/api/v1/mobile/agendas/{agendaId}/voto` | Registro de voto |

As telas retornam os campos, botões, URL e body definidos no Anexo 1. Quando o usuário aciona um botão, o aplicativo envia um `POST` para os endpoints REST já existentes.

O domínio das URLs de callback pode ser alterado para emulador, dispositivo físico ou ambiente remoto:

```powershell
$env:MOBILE_CALLBACK_BASE_URL = "http://192.168.0.10:8080"
docker compose up -d --force-recreate
```

No exemplo, o IP deve ser o endereço da máquina na rede local. Sem Docker, defina a variável antes de executar `mvn spring-boot:run`.
