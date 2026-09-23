# POC — Mutation Testing com PIT (Pitest)

Prova de conceito para apresentação técnica sobre **Mutation Testing**,
usando [PIT (Pitest)](https://pitest.org/) em um projeto **Java 21 + Spring
Boot 3 + Gradle (Kotlin DSL)**.

A POC contém **3 cenários de complexidade crescente**, cada um demonstrando
uma lição diferente sobre a diferença entre *cobertura de linha* (line
coverage) e *qualidade real dos testes* (mutation score).

---

## 1. Como rodar

```bash
./gradlew pitest
```

Isso compila o projeto, roda a suíte de testes JUnit 5 sob o PIT e gera o
relatório de mutação para os 3 cenários de uma vez.

Também existe uma task de conveniência que roda o mesmo pitest e imprime o
caminho do relatório no console ao final:

```bash
./gradlew pitestAll
```

Ou use o script pronto que já roda e abre o relatório no browser:

```bash
./run-demo.sh
```

> Testes "normais" (sem mutação) continuam disponíveis com `./gradlew test`.

---

## 1.1 Comando para usar durante desenvolvimento assistido por IA

Quando a IA gera ou altera um teste, rodar o PIT no projeto inteiro a cada
iteração é lento. Use o parâmetro `-PtargetClass` para mutar **só a classe
que acabou de mudar**:

```bash
./gradlew pitest -PtargetClass=com.example.mutationtest.scenario1.simple.DiscountService
```

Isso roda o PIT apenas contra `DiscountService` + `DiscountServiceTest`, com
feedback em segundos. Além disso, a task já está configurada com
**análise incremental** (`historyInputLocation`/`historyOutputLocation`
apontando para `build/pitest/history.bin`): execuções seguintes reaproveitam
o histórico e ficam ainda mais rápidas.

Fluxo recomendado para validar testes gerados por IA:
1. Peça o teste (ou receba a sugestão da IA).
2. Rode o comando acima apontando para a classe alterada.
3. Abra `build/reports/pitest/index.html` e procure mutantes `SURVIVED`.
4. Peça à IA para reforçar exatamente as asserções relacionadas àquele
   mutante (ex.: "esse boundary `>=` não está sendo testado no valor
   exato do limite") — assim o teste passa a validar comportamento real,
   não só cobrir a linha.
5. Quando terminar, rode `./gradlew pitestAll` para conferir o projeto
   completo antes do commit/PR.

---

## 2. Onde ver o relatório

Após a execução, o relatório HTML fica em:

```
build/reports/pitest/index.html
```

(O plugin está configurado com `timestampedReports = false`, então o
caminho é sempre este mesmo, facilitando abrir o relatório repetidamente
durante uma apresentação ao vivo.)

Um XML equivalente (`mutations.xml`) também é gerado no mesmo diretório,
útil para integração com CI/dashboards.

---

## 3. O que cada cor significa no relatório HTML

O PIT usa um esquema de cores por linha de código-fonte:

| Cor              | Significado                                                                 |
|-------------------|------------------------------------------------------------------------------|
| 🟢 Verde           | Linha coberta por teste **e** todos os mutantes gerados nela foram mortos.   |
| 🔴 Vermelho/Rosa   | Linha coberta por teste, mas **existe pelo menos um mutante sobrevivente**.  |
| ⚪ Sem cor/cinza    | Linha **não coberta** por nenhum teste (não gera mutantes, ou eles não são testados). |

Ao passar o mouse sobre uma linha destacada, o relatório mostra os detalhes
de cada mutante gerado ali: o tipo de mutação aplicada (ex.: boundary
condition, negation, arithmetic, return value) e se ele foi **KILLED**
(morto por algum teste) ou **SURVIVED** (sobreviveu — nenhum teste falhou
mesmo com o código alterado).

---

## 4. Mutation Score vs Line Coverage — como interpretar

- **Line Coverage (cobertura de linha)**: percentual de linhas de código
  executadas por pelo menos um teste. Responde apenas: *"esse código foi
  executado durante os testes?"*
- **Mutation Score**: percentual de mutantes (pequenas alterações no
  bytecode de produção — trocar `>=` por `>`, `+` por `-`, inverter uma
  condição, mudar um valor de retorno, etc.) que foram **detectados**
  (mortos) pelos testes. Responde a uma pergunta muito mais forte:
  *"se esse código estivesse errado, algum teste falharia?"*

**Por que os dois números podem divergir:**
Uma linha pode estar 100% coberta (executada) e, ainda assim, nenhum teste
falhar se o comportamento daquela linha mudar — porque a asserção é fraca,
inexistente, ou não valida o valor exato esperado. Isso é exatamente o que
os Cenários 1 e 2 desta POC demonstram na prática.

Regra prática:
- Line coverage baixo ⇒ com certeza há partes não testadas.
- Line coverage alto/100% ⇒ **não** garante nada sobre a qualidade das
  asserções. É aí que o Mutation Score entra: ele mede a *efetividade* dos
  testes, não apenas sua *extensão*.

---

## 5. Tabela comparativa dos 3 cenários

Números obtidos rodando `./gradlew pitest` neste projeto (mutadores
`STRONGER`):

| Cenário | Classe                        | Line Coverage | Mutation Score | Lição principal                                                                 |
|---------|-------------------------------|:-------------:|:---------------:|----------------------------------------------------------------------------------|
| 1 — Simples       | `DiscountService`             | 93% (13/14)   | **55%** (11/20) | Mesmo sem 100% de cobertura, o ponto central é: os testes que existem para "cliente comum" não usam asserções de valor exato → vários mutantes de boundary/arithmetic sobrevivem. |
| 2 — Intermediário | `CreditEligibilityService`    | **100%** (12/12) | **76%** (19/25) | Cobertura de linha perfeita, mas asserções fracas (`assertNotNull`, "é um dos valores aceitáveis") deixam mutantes de boundary e return-value vivos. |
| 3 — Complexo      | `PricingService`              | **100%** (27/27) | **97%** (29/30) | Testes bem escritos (valores exatos, os dois lados de cada boundary, `@ParameterizedTest`) — o PIT confirma que a suíte realmente protege contra regressões. |

**Leitura da tabela:** repare como o Cenário 2 tem line coverage **igual**
ao Cenário 3 (100%), mas mutation score bem menor (76% vs 97%) — a prova
concreta de que os dois números medem coisas diferentes.

---

## 6. Estrutura do projeto

```
src/main/java/com/example/mutationtest/
├── MutationTestApplication.java                 # Spring Boot app
├── scenario1/simple/
│   └── DiscountService.java                     # Cenário 1
├── scenario2/intermediate/
│   ├── CreditDecision.java
│   └── CreditEligibilityService.java            # Cenário 2
└── scenario3/complex/
    ├── PricingRule.java
    ├── Product.java
    ├── ProductRepository.java / StockRepository.java
    ├── InMemoryProductRepository.java / InMemoryStockRepository.java
    └── PricingService.java                      # Cenário 3

src/test/java/com/example/mutationtest/
├── scenario1/simple/DiscountServiceTest.java
├── scenario2/intermediate/CreditEligibilityServiceTest.java
└── scenario3/complex/PricingServiceTest.java
```

## 7. Configuração do PIT (`build.gradle.kts`)

- `targetClasses` / `targetTests`: `com.example.mutationtest.*` (todos os
  cenários analisados juntos).
- `mutators = ["STRONGER"]`: grupo de mutadores mais agressivo do PIT
  (inclui boundary conditions, negation, arithmetic, return values,
  increments, invert negatives, switch, entre outros).
- `outputFormats = ["HTML", "XML"]`.
- `timestampedReports = false`: relatório sempre no mesmo caminho.
- `failWhenNoMutations = false`.
- `threads = 4`.
- Task customizada `pitestAll`: roda o `pitest` e imprime o caminho do
  relatório HTML ao final, pensada para demos ao vivo.

---

## 8. Principais tipos de mutantes explorados na POC

- **Boundary conditions**: troca de `>=`/`<=` por `>`/`<` (ex.: limite de
  R$500 no Cenário 1, score 700 no Cenário 2, tiers de quantidade e
  estoque no Cenário 3).
- **Negation**: inversão de condições booleanas (ex.: flag `hasActiveDebt`
  no Cenário 2, `quantity <= 0` no Cenário 3).
- **Arithmetic**: troca de operadores matemáticos (`*`, `-`, `+`) nas
  fórmulas de desconto e precificação.
- **Return values**: troca do valor/constante retornado, ou de
  `Math.max` por `Math.min` (Cenário 3).
