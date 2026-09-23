package com.example.mutationtest.scenario2.intermediate;

import org.springframework.stereotype.Service;

/**
 * CENÁRIO 2 — INTERMEDIÁRIO (lógica de negócio real)
 * ---------------------------------------------------------------------
 * Validador de elegibilidade para crédito bancário.
 *
 * OBJETIVO DIDÁTICO PRINCIPAL DESTE CENÁRIO:
 * Mostrar que ASSERÇÕES FRACAS deixam mutantes vivos mesmo quando o
 * teste EXECUTA o caminho correto. Veja CreditEligibilityServiceTest:
 * os testes cobrem todos os ramos (bom para line coverage), mas várias
 * asserções apenas checam "não é nulo" ou "está dentro de um conjunto
 * de valores aceitáveis", em vez de checar o valor EXATO esperado.
 *
 * Regras de negócio:
 * - Score >= 700 AND renda >= 3000: APROVADO
 * - Score >= 700 AND renda <  3000: APROVADO_CONDICIONAL
 * - Score entre 500 e 699 (inclusive) AND sem dívidas ativas: ANALISE_MANUAL
 * - Score < 500: REPROVADO
 * - Qualquer dívida ativa com score < 700: REPROVADO
 * - Parâmetros nulos: lança IllegalArgumentException
 */
@Service
public class CreditEligibilityService {

    static final int SCORE_APPROVAL_THRESHOLD = 700;
    static final int SCORE_MANUAL_REVIEW_THRESHOLD = 500;
    static final double INCOME_THRESHOLD = 3000.0;

    public CreditDecision evaluate(Integer score, Double monthlyIncome, Boolean hasActiveDebt) {
        if (score == null || monthlyIncome == null || hasActiveDebt == null) {
            throw new IllegalArgumentException("score, monthlyIncome and hasActiveDebt must not be null");
        }

        // Regra: qualquer dívida ativa com score < 700 reprova imediatamente.
        // Negation mutant: PIT pode remover a negação de hasActiveDebt.
        // Boundary mutant: PIT pode trocar '<' por '<=' aqui.
        if (hasActiveDebt && score < SCORE_APPROVAL_THRESHOLD) {
            return CreditDecision.REPROVADO;
        }

        // Boundary mutant: '>=' -> '>' em SCORE_APPROVAL_THRESHOLD.
        if (score >= SCORE_APPROVAL_THRESHOLD) {
            // Boundary mutant: '>=' -> '>' em INCOME_THRESHOLD.
            if (monthlyIncome >= INCOME_THRESHOLD) {
                return CreditDecision.APROVADO;
            }
            return CreditDecision.APROVADO_CONDICIONAL;
        }

        // Neste ponto score < 700 e (garantido pelo primeiro if) não há dívida ativa.
        // Boundary mutant: '>=' -> '>' em SCORE_MANUAL_REVIEW_THRESHOLD.
        if (score >= SCORE_MANUAL_REVIEW_THRESHOLD) {
            return CreditDecision.ANALISE_MANUAL;
        }

        return CreditDecision.REPROVADO;
    }
}
