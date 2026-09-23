package com.example.mutationtest.scenario3.complex;

import java.util.Optional;

/**
 * Repositório de produtos. Em produção seria implementado com JPA/JDBC;
 * nesta POC é mockado nos testes (Mockito) para isolar a lógica de
 * precificação de {@link PricingService}.
 */
public interface ProductRepository {

    Optional<Product> findById(String productId);
}
