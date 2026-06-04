---
description: Audita código Java do Zenith Harvest contra os princípios SOLID, com achados por princípio e refactorings concretos.
---

# Revisão SOLID do Zenith Harvest

Use este prompt quando o usuário pedir revisão SOLID ou quando uma classe Java parecer concentrar responsabilidades demais.

## Como auditar

1. Leia os arquivos alvo.
2. Avalie os 5 princípios SOLID.
3. Classifique achados como:
   - 🔴 VIOLAÇÃO CLARA
   - 🟡 SUSPEITA
   - 🟢 OBSERVAÇÃO
   - ✅ OK
4. Para cada achado, informe:
   - arquivo
   - linha, se possível
   - problema
   - refactoring concreto

## O que checar

### S — Single Responsibility

- classe muito grande
- service com responsabilidades demais
- controller fazendo cálculo, persistência ou validação manual demais

### O — Open/Closed

- `switch` ou `if/else` gigante por tipo
- necessidade de editar a mesma classe toda vez que surge um novo tipo

### L — Liskov Substitution

- subclasse que quebra contrato
- `UnsupportedOperationException` em método herdado

### I — Interface Segregation

- interfaces grandes demais
- clientes forçados a depender de métodos que não usam

### D — Dependency Inversion

- service acoplado a implementação concreta
- uso de `new` em dependências que deveriam ser injetadas
- vazamento de detalhes de infra para camadas de negócio

## Formato do relatório

```text
## Revisão SOLID — <arquivo(s)>

### S — Single Responsibility
🔴 / 🟡 / 🟢 / ✅ OK
- [arquivo:linha] Problema. -> Refactoring sugerido.

### O — Open/Closed
...

### L — Liskov Substitution
...

### I — Interface Segregation
...

### D — Dependency Inversion
...

## Resumo
- Violações 🔴: N
- Suspeitas 🟡: N
- Observações 🟢: N

Veredito: APROVADO / APROVADO COM AJUSTES / REFATORAR ANTES DE SEGUIR
```

Não invente violações. Se estiver OK, diga explicitamente.
