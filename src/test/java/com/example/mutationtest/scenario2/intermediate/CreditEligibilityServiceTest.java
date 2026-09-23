package com.example.mutationtest.scenario2.intermediate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CENÁRIO 2 — INTERMEDIÁRIO
 * ---------------------------------------------------------------------
 * ATENÇÃO: estes testes cobrem todos os caminhos (bom para line coverage),
 * mas propositalmente usam ASSERÇÕES FRACAS em vários casos, para que o
 * PIT demonstre mutantes sobreviventes mesmo com "boa" cobertura de linha.
 */
@DisplayName("CreditEligibilityService - Cenário 2 (Intermediário)")
class CreditEligibilityServiceTest {

    private final CreditEligibilityService service = new CreditEligibilityService();

    @Nested
    @DisplayName("Validação de entrada")
    class InputValidation {

        @Test
        @DisplayName("Score nulo deve lançar IllegalArgumentException")
        void nullScoreThrowsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> service.evaluate(null, 3000.0, false));
        }

        @Test
        @DisplayName("Renda nula deve lançar IllegalArgumentException")
        void nullIncomeThrowsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> service.evaluate(700, null, false));
        }

        @Test
        @DisplayName("Flag de dívida ativa nula deve lançar IllegalArgumentException")
        void nullDebtFlagThrowsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> service.evaluate(700, 3000.0, null));
        }
    }

    @Nested
    @DisplayName("[GAP] Score alto - asserções fracas (apenas 'não é nulo' ou 'é um dos aprovados')")
    class HighScoreWeakAssertions {

        // GAP: verifica apenas que o resultado não é nulo. Um mutante que troque
        // APROVADO por APROVADO_CONDICIONAL (return value mutant) sobrevive.
        @Test
        @DisplayName("[GAP] Score >= 700 e renda >= 3000 deve aprovar (só checa não-nulo)")
        void highScoreHighIncomeReturnsNonNullDecision() {
            CreditDecision decision = service.evaluate(750, 5000.0, false);

            assertNotNull(decision);
        }

        // GAP: aceita qualquer decisão "positiva", não valida qual delas é a
        // correta. Isso mascara o boundary mutant em INCOME_THRESHOLD (>= -> >),
        // pois tanto APROVADO quanto APROVADO_CONDICIONAL estão no conjunto aceito.
        @Test
        @DisplayName("[GAP] Score >= 700 e renda < 3000 deve aprovar condicionalmente (aceita qualquer aprovação)")
        void highScoreLowIncomeReturnsSomeApprovalDecision() {
            CreditDecision decision = service.evaluate(750, 1000.0, false);

            assertTrue(decision == CreditDecision.APROVADO
                    || decision == CreditDecision.APROVADO_CONDICIONAL);
        }
    }

    @Nested
    @DisplayName("Regras com asserções fortes (mata mutantes)")
    class StrongAssertions {

        @Test
        @DisplayName("Score < 500 deve reprovar (valor exato, mata return-value mutant)")
        void lowScoreIsRejected() {
            CreditDecision decision = service.evaluate(300, 5000.0, false);

            assertEquals(CreditDecision.REPROVADO, decision);
        }

        @Test
        @DisplayName("Score entre 500 e 699 sem dívida ativa vai para análise manual (valor exato)")
        void midScoreNoDebtGoesToManualReview() {
            CreditDecision decision = service.evaluate(600, 2000.0, false);

            assertEquals(CreditDecision.ANALISE_MANUAL, decision);
        }

        @Test
        @DisplayName("Dívida ativa com score < 700 reprova mesmo com renda alta (mata negation mutant)")
        void activeDebtWithLowScoreIsRejectedRegardlessOfIncome() {
            CreditDecision decision = service.evaluate(650, 10000.0, true);

            assertEquals(CreditDecision.REPROVADO, decision);
        }

        // GAP: apenas garante que a decisão é DIFERENTE de REPROVADO, sem checar
        // exatamente qual decisão é. Mutante que troca APROVADO/APROVADO_CONDICIONAL
        // entre si sobrevive; mutante de boundary em SCORE_APPROVAL_THRESHOLD que
        // ainda resulte numa aprovação também sobrevive.
        @Test
        @DisplayName("[GAP] Dívida ativa com score >= 700 não deve ser automaticamente reprovado")
        void activeDebtWithHighScoreIsNotAutomaticallyRejected() {
            CreditDecision decision = service.evaluate(750, 5000.0, true);

            assertNotEquals(CreditDecision.REPROVADO, decision);
        }
    }
}
