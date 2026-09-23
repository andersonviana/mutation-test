package com.example.mutationtest.scenario1.simple;

import org.springframework.stereotype.Service;

/**
 * CENÁRIO 1 — SIMPLES (dia a dia)
 * ---------------------------------------------------------------------
 * Serviço de cálculo de desconto em e-commerce.
 *
 * OBJETIVO DIDÁTICO PRINCIPAL DESTE CENÁRIO:
 * Mostrar que 100% de LINE COVERAGE não garante qualidade de teste.
 * Veja DiscountServiceTest: os testes executam todas as linhas deste
 * arquivo (line coverage alto/100%), mas para o caso de "cliente comum"
 * as asserções são fracas/ausentes, permitindo que vários MUTANTES
 * sobrevivam (boundary conditions e arithmetic principalmente).
 *
 * Regras de negócio:
 * - Cliente VIP com compra >= R$500: 20% de desconto
 * - Cliente VIP com compra <  R$500: 10% de desconto
 * - Cliente comum com compra >= R$500: 5% de desconto
 * - Cliente comum com compra <  R$500: sem desconto
 * - Valor negativo: lança IllegalArgumentException
 */
@Service
public class DiscountService {

    // Boundary condition: PIT vai mutar ">=" para "<", "<=", ">", etc.
    // Um bom teste precisa validar o valor EXATAMENTE em 500.0 (boundary).
    static final double VIP_THRESHOLD = 500.0;
    static final double VIP_HIGH_DISCOUNT = 0.20;
    static final double VIP_LOW_DISCOUNT = 0.10;
    static final double REGULAR_HIGH_DISCOUNT = 0.05;
    static final double REGULAR_NO_DISCOUNT = 0.0;

    /**
     * Calcula o preço final após aplicar o desconto de acordo com o
     * perfil do cliente (VIP ou comum) e o valor total da compra.
     *
     * @param vipCustomer    indica se o cliente é VIP
     * @param purchaseAmount valor total da compra (deve ser >= 0)
     * @return preço final já com o desconto aplicado
     * @throws IllegalArgumentException se purchaseAmount for negativo
     */
    public double calculateFinalPrice(boolean vipCustomer, double purchaseAmount) {
        if (purchaseAmount < 0) {
            throw new IllegalArgumentException("Purchase amount cannot be negative");
        }

        double discountRate = calculateDiscountRate(vipCustomer, purchaseAmount);

        // Arithmetic mutant: PIT pode trocar '-' por '+' aqui.
        // Return value mutant: PIT pode trocar o retorno por 0 ou pelo próprio input.
        return purchaseAmount * (1 - discountRate);
    }

    /**
     * Calcula apenas a taxa de desconto (0.0 a 0.20) sem aplicar ao valor.
     * Exposto separadamente para facilitar testes de boundary conditions.
     */
    public double calculateDiscountRate(boolean vipCustomer, double purchaseAmount) {
        if (purchaseAmount < 0) {
            throw new IllegalArgumentException("Purchase amount cannot be negative");
        }

        if (vipCustomer) {
            // Negation mutant: PIT pode inverter a condição >=.
            if (purchaseAmount >= VIP_THRESHOLD) {
                return VIP_HIGH_DISCOUNT;
            }
            return VIP_LOW_DISCOUNT;
        }

        // ATENÇÃO: os testes fracos/ausentes para este ramo (cliente comum)
        // são o coração do objetivo didático deste cenário.
        if (purchaseAmount >= VIP_THRESHOLD) {
            return REGULAR_HIGH_DISCOUNT;
        }
        return REGULAR_NO_DISCOUNT;
    }
}
