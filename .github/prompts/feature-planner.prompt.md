---
description: Planeja uma feature do Zenith Harvest antes da implementação, cobrindo serviços, entidades, endpoints, fluxo e eventos.
---

# Planejar feature do Zenith Harvest

Use este prompt antes de começar uma feature nova ou quando a solicitação ainda estiver ambígua.

Este prompt **planeja**, não implementa.

## Contexto fixo

- `gateway` (8080): borda e JWT
- `core-svc` (8081): CRUD, auth, financeiro e publicação de eventos
- `analise-svc` (8082): satélite, IA e consumo de eventos
- comunicação por Feign e RabbitMQ
- Postgres como fonte relacional e MongoDB para NDVI/auditoria detalhada

## Como planejar

1. Leia `CLAUDE.md`, `copilot-instructions.md` e o schema SQL.
2. Identifique quais serviços serão afetados.
3. Produza um plano técnico antes de escrever código.
4. Deixe explícitas ambiguidades, decisões em aberto e riscos.

## Estrutura esperada do plano

```text
## Feature: <nome>

### Resumo

### Serviço(s) afetado(s)

### Entidades / tabelas envolvidas

### Endpoints REST

### Fluxo

### Comunicação entre serviços

### Persistência NoSQL

### Regras de negócio / validações

### Decisões em aberto / riscos

### Ordem de implementação sugerida
```

## Princípios

- não inventar entidade se o schema já cobre o caso
- sinalizar claramente quando a feature exigir mudança de schema
- respeitar o menor caminho que entrega valor
- sempre terminar com ordem de implementação sugerida
