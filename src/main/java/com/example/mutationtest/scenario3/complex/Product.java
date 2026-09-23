package com.example.mutationtest.scenario3.complex;

/**
 * Representa um produto do catálogo para fins de precificação.
 *
 * @param id        identificador único do produto
 * @param name      nome do produto
 * @param basePrice preço base (antes de regras de categoria/quantidade)
 * @param category  regra de precificação aplicável (ver {@link PricingRule})
 */
public record Product(String id, String name, double basePrice, PricingRule category) {
}
