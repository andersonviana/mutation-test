package com.example.mutationtest.scenario2.intermediate;

/**
 * Resultado possível da avaliação de elegibilidade de crédito.
 * Ver {@link CreditEligibilityService}.
 */
public enum CreditDecision {
    APROVADO,
    APROVADO_CONDICIONAL,
    ANALISE_MANUAL,
    REPROVADO
}
