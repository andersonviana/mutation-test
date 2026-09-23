package com.example.mutationtest.scenario3.complex;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementação em memória de {@link StockRepository}, usada apenas para
 * permitir que o contexto Spring suba fora dos testes unitários, onde a
 * interface é mockada com Mockito.
 */
@Repository
public class InMemoryStockRepository implements StockRepository {

    private final Map<String, Integer> stockByProductId = new ConcurrentHashMap<>(Map.of(
            "prod-1", 100,
            "prod-2", 100,
            "prod-3", 5
    ));

    @Override
    public int getStock(String productId) {
        return stockByProductId.getOrDefault(productId, 0);
    }
}
