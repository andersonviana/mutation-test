package com.example.mutationtest.scenario1.simple;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CENÁRIO 1 — SIMPLES
 * ---------------------------------------------------------------------
 * ATENÇÃO: estes testes são PROPOSITALMENTE incompletos para a demo.
 *
 * Objetivo didático: mostrar que 100% de LINE COVERAGE (todas as linhas
 * de DiscountService são executadas por estes testes) NÃO garante que
 * o comportamento esteja realmente validado. O PIT vai revelar mutantes
 * sobreviventes exatamente nos pontos marcados como "GAP" abaixo.
 */
@DisplayName("DiscountService - Cenário 1 (Simples)")
class DiscountServiceTest {

    private final DiscountService service = new DiscountService();

    @Nested
    @DisplayName("Cliente VIP - regra bem coberta (asserções fortes)")
    class VipCustomerRules {

        @Test
        @DisplayName("VIP com compra >= 500 deve receber 20% de desconto (mata boundary e arithmetic mutants)")
        void vipWithHighPurchaseGetsTwentyPercentOff() {
            double result = service.calculateFinalPrice(true, 1000.0);

            // Asserção forte com valor exato: mata mutantes de arithmetic
            // (troca de '-' por '+'/'*') e de return value.
            assertEquals(800.0, result);
        }

        @Test
        @DisplayName("VIP exatamente no limite de 500 deve receber 20% (mata boundary mutant >= -> >)")
        void vipAtExactThresholdGetsTwentyPercentOff() {
            double result = service.calculateFinalPrice(true, 500.0);

            assertEquals(400.0, result, 0.0001);
        }

        @Test
        @DisplayName("VIP logo abaixo do limite (499.99) deve receber apenas 10% (mata boundary mutant >= -> <=)")
        void vipJustBelowThresholdGetsTenPercentOff() {
            double result = service.calculateFinalPrice(true, 499.99);

            assertEquals(499.99 * 0.9, result, 0.0001);
        }

        @Test
        @DisplayName("VIP com compra baixa deve receber 10% de desconto")
        void vipWithLowPurchaseGetsTenPercentOff() {
            double result = service.calculateFinalPrice(true, 100.0);

            assertEquals(90.0, result, 0.0001);
        }
    }

    @Nested
    @DisplayName("Validação de entrada")
    class InputValidation {

        @Test
        @DisplayName("Valor negativo deve lançar IllegalArgumentException")
        void negativeAmountThrowsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> service.calculateFinalPrice(true, -1.0));
        }
    }

    @Nested
    @DisplayName("Cliente comum - *** GAP PROPOSITAL: asserções fracas/ausentes ***")
    class RegularCustomerRules {

        // GAP #1: este teste executa a linha de "cliente comum >= 500" (contribuindo
        // para 100% de line coverage), mas a asserção não verifica o valor exato do
        // desconto (5%). Qualquer mutante que altere REGULAR_HIGH_DISCOUNT,
        // o boundary ">=" ou a fórmula aritmética SOBREVIVE, pois "result < amount"
        // continua verdadeiro mesmo com o mutante.
        @Test
        @DisplayName("[GAP] Cliente comum com compra alta: só verifica que houve ALGUM desconto")
        void regularCustomerWithHighPurchaseHasSomeDiscount() {
            double result = service.calculateFinalPrice(false, 1000.0);

            // Asserção fraca: não mata boundary/arithmetic/return-value mutants
            // (o valor correto seria 950.0, mas não verificamos isso aqui).
            assertTrue(result <= 1000.0);
        }

        // GAP #2: este teste só garante que o método "não explode" (não lança
        // exceção) para o caso de cliente comum com compra baixa. Não valida que
        // o desconto deveria ser ZERO. Mutantes que trocam REGULAR_NO_DISCOUNT
        // por qualquer outro valor, ou que invertem a condição, sobrevivem.
        @Test
        @DisplayName("[GAP] Cliente comum com compra baixa: só verifica ausência de exceção")
        void regularCustomerWithLowPurchaseDoesNotThrow() {
            assertDoesNotThrow(() -> service.calculateFinalPrice(false, 100.0));
        }

        // Não há nenhum teste de boundary exato (amount == 500.0) para cliente
        // comum, nem verificação da taxa via calculateDiscountRate(). Isso é
        // proposital: reforça o GAP de cobertura de qualidade (não de linha).
    }
}
