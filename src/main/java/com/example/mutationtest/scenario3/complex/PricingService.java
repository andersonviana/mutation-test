package com.example.mutationtest.scenario3.complex;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * CENÁRIO 3 — COMPLEXO (padrão real de produção)
 * ---------------------------------------------------------------------
 * Sistema de precificação dinâmica com Spring Service, integração com
 * repositórios (mockados nos testes) e regras compostas.
 *
 * OBJETIVO DIDÁTICO PRINCIPAL DESTE CENÁRIO:
 * Mostrar o PIT em sua MELHOR forma: quando os testes são bem escritos
 * (asserções fortes, boundaries testados dos dois lados, caminhos de
 * exceção cobertos), a mutation score fica alta e próxima da line
 * coverage — reforçando a confiança real nos testes.
 *
 * Regras de negócio:
 * - Produto PREMIUM: basePrice * 1.5
 * - Produto SEASONAL com estoque < 10: basePrice * 2.0
 * - Produto SEASONAL com estoque >= 10: basePrice * 1.2
 * - Produto STANDARD: basePrice (sem ajuste de categoria)
 * - Desconto progressivo por quantidade: 1-4 unid = 0%, 5-9 = 5%,
 *   10-19 = 10%, 20+ = 15%
 * - Preço mínimo nunca pode ser menor que basePrice * 0.8 * quantidade
 * - Lança exceção para quantidade <= 0
 */
@Service
public class PricingService {

    static final double PREMIUM_MULTIPLIER = 1.5;
    static final double SEASONAL_LOW_STOCK_MULTIPLIER = 2.0;
    static final double SEASONAL_NORMAL_STOCK_MULTIPLIER = 1.2;
    static final int SEASONAL_LOW_STOCK_THRESHOLD = 10;

    static final int BULK_DISCOUNT_TIER_1_QTY = 5;   // 5-9 unidades
    static final int BULK_DISCOUNT_TIER_2_QTY = 10;  // 10-19 unidades
    static final int BULK_DISCOUNT_TIER_3_QTY = 20;  // 20+ unidades

    static final double BULK_DISCOUNT_TIER_1_RATE = 0.05;
    static final double BULK_DISCOUNT_TIER_2_RATE = 0.10;
    static final double BULK_DISCOUNT_TIER_3_RATE = 0.15;
    static final double NO_DISCOUNT_RATE = 0.0;

    static final double MIN_PRICE_FACTOR = 0.8;

    private final ProductRepository productRepository;
    private final StockRepository stockRepository;

    @Autowired
    public PricingService(ProductRepository productRepository, StockRepository stockRepository) {
        this.productRepository = productRepository;
        this.stockRepository = stockRepository;
    }

    /**
     * Calcula o preço total para uma dada quantidade de um produto,
     * aplicando regras de categoria (PricingRule), desconto progressivo
     * por quantidade e o piso mínimo de preço.
     *
     * @param productId identificador do produto no catálogo
     * @param quantity  quantidade desejada (deve ser > 0)
     * @return preço total (unitPrice ajustado * quantidade * desconto),
     *         nunca menor que basePrice * 0.8 * quantidade
     * @throws IllegalArgumentException se quantity <= 0
     * @throws IllegalStateException    se o produto não existir no catálogo
     */
    public double calculatePrice(String productId, int quantity) {
        // Negation / boundary mutant: PIT pode trocar '<=' por '<' ou remover o if.
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalStateException("Product not found: " + productId));

        double unitPrice = calculateUnitPrice(product);
        double discountRate = calculateBulkDiscountRate(quantity);

        // Arithmetic mutants: PIT pode mutar '*' e '-' nesta expressão.
        double totalWithDiscount = unitPrice * quantity * (1 - discountRate);

        double minimumTotal = product.basePrice() * MIN_PRICE_FACTOR * quantity;

        // Return value mutant: PIT pode trocar Math.max por Math.min.
        return Math.max(totalWithDiscount, minimumTotal);
    }

    /**
     * Aplica o ajuste de preço unitário de acordo com a categoria do
     * produto (PricingRule) e, no caso SEASONAL, com o nível de estoque.
     */
    double calculateUnitPrice(Product product) {
        return switch (product.category()) {
            case PREMIUM -> product.basePrice() * PREMIUM_MULTIPLIER;
            case SEASONAL -> {
                int stock = stockRepository.getStock(product.id());
                // Boundary mutant: PIT pode trocar '<' por '<=' aqui.
                if (stock < SEASONAL_LOW_STOCK_THRESHOLD) {
                    yield product.basePrice() * SEASONAL_LOW_STOCK_MULTIPLIER;
                }
                yield product.basePrice() * SEASONAL_NORMAL_STOCK_MULTIPLIER;
            }
            case STANDARD -> product.basePrice();
        };
    }

    /**
     * Calcula a taxa de desconto progressivo por quantidade.
     * 1-4 = 0%, 5-9 = 5%, 10-19 = 10%, 20+ = 15%.
     */
    double calculateBulkDiscountRate(int quantity) {
        // Boundary mutants: PIT pode trocar '>=' por '>' em cada um destes ifs,
        // deslocando as fronteiras de tier (5, 10, 20) em uma unidade.
        if (quantity >= BULK_DISCOUNT_TIER_3_QTY) {
            return BULK_DISCOUNT_TIER_3_RATE;
        }
        if (quantity >= BULK_DISCOUNT_TIER_2_QTY) {
            return BULK_DISCOUNT_TIER_2_RATE;
        }
        if (quantity >= BULK_DISCOUNT_TIER_1_QTY) {
            return BULK_DISCOUNT_TIER_1_RATE;
        }
        return NO_DISCOUNT_RATE;
    }
}
