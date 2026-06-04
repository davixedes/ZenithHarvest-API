---
description: Revisa código Java do Zenith Harvest contra os padrões do projeto antes de commit, PR ou entrega.
---

# Revisão de código Java do Zenith Harvest

Use este prompt após alterar classes Java do projeto, especialmente:

- entity
- controller
- service
- DTO
- mapper
- config

## Como revisar

1. Leia o `CLAUDE.md` e o `copilot-instructions.md` para relembrar os padrões do projeto.
2. Revise os arquivos alterados.
3. Reporte por severidade:
   - 🔴 BLOQUEIA
   - 🟡 AJUSTAR
   - 🟢 SUGESTÃO
4. Para cada achado, informe:
   - arquivo
   - linha, se possível
   - problema
   - correção concreta

## Checklist do projeto

### Arquitetura

- DTOs na fronteira da API; nunca serializar entity direto
- controller -> service -> repository
- gateway não pode receber Spring MVC

### Persistência

- dinheiro em `BigDecimal`
- PK correta (`UUID` para entidade, `Integer` para lookup)
- nomes de tabela/coluna idênticos ao schema
- `nullable = false` apenas onde faz sentido na criação
- `*Situation`, nunca `*Status`
- `ddl-auto: validate`, nunca `update`

### Segurança

- sem segredo hardcoded
- endpoints protegidos por JWT, salvo públicos explícitos
- sem log de dado sensível

### Requisitos do projeto

- HATEOAS no `core-svc`
- `@Cacheable` onde houver baixa volatilidade
- CORS configurado
- endpoints claros e documentáveis

### Qualidade geral

- sem Lombok
- `@Valid` nos requests
- tratamento de erro centralizado
- sem código morto nem `System.out.println`

## Formato do relatório

```text
## Revisão — <arquivo(s)>

🔴 BLOQUEIA (N)
- [arquivo:linha] Problema. -> Correção.

🟡 AJUSTAR (N)
- ...

🟢 SUGESTÃO (N)
- ...

Veredito: APROVADO / APROVADO COM AJUSTES / BLOQUEADO
```

Se estiver tudo certo, diga claramente que está aprovado e por quê.
