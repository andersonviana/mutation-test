package com.example.mutationtest.scenario3.complex;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementação em memória de {@link ProductRepository}, usada apenas para
 * permitir que o contexto Spring suba (ex.: MutationTestApplication) fora
 * dos testes unitários, onde a interface é mockada com Mockito.
 */
@Repository
public class InMemoryProductRepository implements ProductRepository {

    private final Map<String, Product> products = new ConcurrentHashMap<>(Map.of(
            "prod-1", new Product("prod-1", "Standard Widget", 100.0, PricingRule.STANDARD),
            "prod-2", new Product("prod-2", "Premium Widget", 200.0, PricingRule.PREMIUM),
            "prod-3", new Product("prod-3", "Seasonal Widget", 50.0, PricingRule.SEASONAL)
    ));

    @Override
    public Optional<Product> findById(String productId) {
        return Optional.ofNullable(products.get(productId));
    }
}
