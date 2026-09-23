package com.example.mutationtest.scenario3.complex;

/**
 * Repositório de estoque, consultado separadamente do catálogo de produtos
 * (simula uma integração real com outro serviço/tabela de estoque).
 * Mockado nos testes de {@link PricingService}.
 */
public interface StockRepository {

    int getStock(String productId);
}
