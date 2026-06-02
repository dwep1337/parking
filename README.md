# Sistema de Estacionamento Estapar

Backend em Java 21 + Spring Boot 4 para gerenciamento de vagas, processamento de eventos do simulador e consulta de faturamento.

## Pré-requisitos

**Docker (recomendado):** Docker e Docker Compose v2.

**Execução local:** Java 21, Maven 3.9+, Docker (apenas para o MySQL e simulador).

## Como executar

### Opção A — Tudo com Docker Compose (recomendado)

Sobe MySQL, simulador e backend na mesma rede Docker. Não exige Java nem Maven instalados na máquina.

```bash
docker compose up --build -d
```

Aguarde os três serviços ficarem saudáveis (a primeira build pode levar alguns minutos):

```bash
docker compose ps
```

Validação:

```bash
curl -s "http://localhost:3003/revenue?date=2025-01-01&sector=A"
curl -s http://localhost:3000/garage | head
```

| Serviço   | URL / porta host      |
| --------- | --------------------- |
| Backend   | http://localhost:3003 |
| Simulador | http://localhost:3000 |
| MySQL     | localhost:3306        |

Parar e remover containers:

```bash
docker compose down
```

Remover também o volume do banco:

```bash
docker compose down -v
```

O perfil `docker` ([`application-docker.yml`](src/main/resources/application-docker.yml)) aponta o backend para `mysql` e `garage-sim` pelos nomes dos serviços. O simulador recebe `CLIENT_WEBHOOK_URL=http://parking:3003/webhook` via Compose.

### Opção B — Desenvolvimento local

#### 1. Subir o MySQL

```bash
docker compose up -d mysql
```

#### 2. Subir o simulador

**Linux:**

```bash
docker run -d --name garage-sim --network=host cfontes0estapar/garage-sim:1.0.0
```

O simulador expõe `GET /garage` na porta **3000** e envia webhooks para `http://localhost:3003/webhook`.

#### 3. Configuração

Em [`src/main/resources/application.yml`](src/main/resources/application.yml) (já configurado para localhost):

```yaml
server:
  port: 3003
simulator:
  base-url: http://localhost:3000
```

#### 4. Build e execução

```bash
mvn clean install
mvn spring-boot:run
```

Na subida, a aplicação sincroniza setores e vagas via `GET /garage`.

## Endpoints

| Método | URL        | Descrição                    |
| ------ | ---------- | ---------------------------- |
| POST   | `/webhook` | Eventos ENTRY, PARKED, EXIT  |
| GET    | `/revenue` | Faturamento do setor na data |

### Webhook

O simulador envia `POST /webhook` (porta **3003**). A resposta é sempre **HTTP 200**, conforme o enunciado — mesmo quando a regra de negócio impede a operação (lotado, placa duplicada, veículo inexistente). Nesses casos o evento é ignorado e registrado em log (nível WARN).

### Exemplo de receita

Query string:

```bash
curl "http://localhost:3003/revenue?date=2025-01-01&sector=A"
```

Corpo JSON (também suportado no GET, conforme enunciado):

```bash
curl -X GET "http://localhost:3003/revenue" \
  -H "Content-Type: application/json" \
  -d '{"date":"2025-01-01","sector":"A"}'
```

```json
{
  "amount": 0.0,
  "currency": "BRL",
  "timestamp": "2025-01-01T12:00:00.000Z"
}
```

## Decisões de modelagem

### Ocupação e preço dinâmico

```
ocupacao = vagasOcupadas / maxCapacity
```

- `vagasOcupadas`: veículos com status `ENTERED` ou `PARKED` no setor.
- `maxCapacity`: valor do simulador (`max_capacity`), não a contagem de vagas físicas.

| Ocupação        | Ajuste        |
| --------------- | ------------- |
| &lt; 25%        | −10%          |
| 25% a 50%       | sem alteração |
| &gt; 50% a 75%  | +10%          |
| &gt; 75% a 100% | +25%          |

Fronteiras inclusivas: exatamente 25% → preço normal; 50% → normal; 75% → +10%.

A tarifa horária efetiva (`hourlyRate`) é **persistida no ENTRY** e reutilizada no EXIT.

### Cobrança

Permanências de até 30 minutos são gratuitas. Ao ultrapassar esse período, a cobrança considera o tempo total de permanência arredondado para cima em horas inteiras.

Exemplo:

```text
30 min → grátis
31 min → 1h
61 min → 2h
121 min → 3h
```

### Lotação

Quando `vagasOcupadas >= maxCapacity` no setor, novas entradas são **rejeitadas** (sem persistir vaga/veículo). O webhook responde **200** e registra o motivo no log.

### ENTRY sem setor no payload

Escolhe o **primeiro setor** (ordem alfabética) com capacidade disponível e vaga `AVAILABLE`.

### Seleção de vaga

Primeira vaga disponível por `id` ascendente no setor escolhido.

### PARKED

Associa vaga por `lat`/`lng` exatos. Se diferente da reservada no ENTRY, libera a antiga e ocupa a nova.

### Receita

Registros na tabela `revenue` com `created_at = exit_time`. A API soma valores onde `sector` e a data UTC de `created_at` coincidem com o parâmetro `date`.

### Timestamp da API `/revenue`

Sempre `date` às **12:00:00 UTC**, conforme exemplo do enunciado.

### Datas do simulador

O webhook aceita `Instant` com sufixo `Z` ou sem timezone (tratado como UTC), ex.: `2026-06-02T01:54:01`.

### Placa duplicada

Nova ENTRY com sessão ativa (`ENTERED`/`PARKED`) é ignorada; o webhook responde **200**.

### Sincronização na subida

`ApplicationRunner` em `GarageService` com retry infinito (intervalo 5s) até o simulador responder.

## Testes

Testes unitários e de controller (JUnit 5, Mockito, MockMvc):

```bash
mvn test
```

## Estrutura

```
com.estapar
├── controller   → WebhookController, RevenueController
├── service      → GarageService, ParkingService, PricingService, RevenueService
├── repository   → JPA repositories
├── entity       → GarageSector, ParkingSpot, Vehicle, Revenue
├── dto          → eventos e respostas
├── client       → GarageSimulatorClient
├── config       → RestClient, Jackson, propriedades
└── exception    → handler global e exceções de negócio
```
