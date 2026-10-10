package group2d.promo_graud.modules.rules.engine;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import group2d.promo_graud.modules.orders.Order;
import group2d.promo_graud.modules.orders.OrderItem;
import group2d.promo_graud.modules.products.entity.Product;
import group2d.promo_graud.modules.rules.RuleCampaign;
import group2d.promo_graud.modules.rules.engine.strategy.BuyXGetYStrategy;
import group2d.promo_graud.modules.rules.engine.strategy.FixedStrategy;
import group2d.promo_graud.modules.rules.engine.strategy.PercentageStrategy;
import group2d.promo_graud.modules.rules.engine.strategy.TieredStrategy;
import group2d.promo_graud.modules.rules.enums.RuleCampaignErrorCode;
import group2d.promo_graud.modules.rules.enums.TypeOfRule;
import group2d.promo_graud.shared.exception.AppException;

@DisplayName("Strategy Tests")
class StrategyTest {

    @Nested
    @DisplayName("PercentageStrategy")
    class PercentageStrategyTests {

        private final PercentageStrategy strategy = new PercentageStrategy();

        @Test
        @DisplayName("getRuleType trả về PERCENTAGE")
        void returns_correct_rule_type() {
            assertEquals(TypeOfRule.PERCENTAGE, strategy.getRuleType());
        }

        @Test
        @DisplayName("Tính đúng discount theo phần trăm")
        void calculates_percentage_discount() {
            Order order = orderOf(1000000);
            RuleCampaign rule =
                    RuleCampaign.builder()
                            .value(BigDecimal.valueOf(20))
                            .maxDiscountValue(BigDecimal.valueOf(300000))
                            .build();

            assertEquals(
                    BigDecimal.valueOf(200000).setScale(2),
                    strategy.calculateDiscount(order, rule));
        }

        @Test
        @DisplayName("Không có maxDiscountValue thì không cap")
        void no_max_discount_no_cap() {
            Order order = orderOf(1000000);
            RuleCampaign rule =
                    RuleCampaign.builder()
                            .value(BigDecimal.valueOf(20))
                            .maxDiscountValue(null)
                            .build();

            assertEquals(
                    BigDecimal.valueOf(200000).setScale(2),
                    strategy.calculateDiscount(order, rule));
        }
    }

    @Nested
    @DisplayName("FixedStrategy")
    class FixedStrategyTests {

        private final FixedStrategy strategy = new FixedStrategy();

        @Test
        @DisplayName("getRuleType trả về FIXED_AMOUNT")
        void returns_correct_rule_type() {
            assertEquals(TypeOfRule.FIXED_AMOUNT, strategy.getRuleType());
        }

        @Test
        @DisplayName("Trả về đúng giá trị cố định khi đơn hàng lớn hơn discount")
        void returns_fixed_value() {
            Order order = orderOf(500000);
            RuleCampaign rule = RuleCampaign.builder().value(BigDecimal.valueOf(50000)).build();

            assertEquals(BigDecimal.valueOf(50000), strategy.calculateDiscount(order, rule));
        }

        @Test
        @DisplayName("Discount bị cap bằng totalPrice khi rule.value lớn hơn đơn hàng")
        void discount_capped_at_order_total() {
            Order order = orderOf(50000);
            RuleCampaign rule =
                    RuleCampaign.builder()
                            .value(BigDecimal.valueOf(100000)) // discount > đơn hàng
                            .build();

            // Không trả về âm
            assertEquals(BigDecimal.valueOf(50000), strategy.calculateDiscount(order, rule));
        }
    }

    @Nested
    @DisplayName("TieredStrategy")
    class TieredStrategyTests {

        private final TieredStrategy strategy = new TieredStrategy();

        @Test
        @DisplayName("getRuleType trả về TIERED")
        void returns_correct_rule_type() {
            assertEquals(TypeOfRule.TIERED, strategy.getRuleType());
        }

        @Test
        @DisplayName("Chọn tier cao nhất mà đơn hàng đủ điều kiện")
        void selects_highest_qualifying_tier() {
            Order order = orderOf(1200000);
            RuleCampaign rule =
                    RuleCampaign.builder()
                            .payload(
                                    tieredPayload(
                                            tier(500000, 30000),
                                            tier(1000000, 80000),
                                            tier(2000000, 150000)))
                            .build();

            assertEquals(BigDecimal.valueOf(80000), strategy.calculateDiscount(order, rule));
        }

        @Test
        @DisplayName("Trả về ZERO khi đơn hàng không đủ điều kiện tier nào")
        void returns_zero_when_no_tier_qualifies() {
            Order order = orderOf(100000);
            RuleCampaign rule =
                    RuleCampaign.builder().payload(tieredPayload(tier(500000, 50000))).build();

            assertEquals(BigDecimal.ZERO, strategy.calculateDiscount(order, rule));
        }

        @Test
        @DisplayName("Trả về ZERO khi payload không có tiers")
        void returns_zero_when_tiers_empty() {
            Order order = orderOf(1000000);
            Map<String, Object> payload = new HashMap<>();
            payload.put("tiers", List.of());
            RuleCampaign rule = RuleCampaign.builder().payload(payload).build();

            assertEquals(BigDecimal.ZERO, strategy.calculateDiscount(order, rule));
        }

        @Test
        @DisplayName("Trả về ZERO khi key 'tiers' null")
        void returns_zero_when_tiers_null() {
            Order order = orderOf(1000000);
            Map<String, Object> payload = new HashMap<>();
            payload.put("tiers", null);
            RuleCampaign rule = RuleCampaign.builder().payload(payload).build();

            assertEquals(BigDecimal.ZERO, strategy.calculateDiscount(order, rule));
        }

        private Map<String, Object> tier(long minOrder, long discount) {
            Map<String, Object> tier = new HashMap<>();
            tier.put("min_order_value", minOrder);
            tier.put("discount_value", discount);
            return tier;
        }

        @SafeVarargs
        private Map<String, Object> tieredPayload(Map<String, Object>... tiers) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("tiers", List.of(tiers));
            return payload;
        }
    }

    @Nested
    @DisplayName("BuyXGetYStrategy")
    class BuyXGetYStrategyTests {

        private final BuyXGetYStrategy strategy = new BuyXGetYStrategy();

        @Test
        @DisplayName("getRuleType trả về BUY_X_GET_Y")
        void returns_correct_rule_type() {
            assertEquals(TypeOfRule.BUY_X_GET_Y, strategy.getRuleType());
        }

        @Test
        @DisplayName("Tính đúng discount: mua 2 tai nghe, tặng 1 cáp sạc")
        void calculates_correct_discount() {
            RuleCampaign rule = RuleCampaign.builder().payload(buyXGetYPayload(1, 2, 3, 1)).build();

            Order order = orderWithItems(itemOf(1, 4, 200000), itemOf(3, 1, 50000));

            assertEquals(BigDecimal.valueOf(100000), strategy.calculateDiscount(order, rule));
        }

        @Test
        @DisplayName("Trả về ZERO khi đơn không có sản phẩm X")
        void returns_zero_when_buy_product_not_in_order() {
            RuleCampaign rule = RuleCampaign.builder().payload(buyXGetYPayload(1, 2, 3, 1)).build();

            Order order = orderWithItems(itemOf(3, 1, 50000)); // chỉ có sản phẩm Y, không có X

            assertEquals(BigDecimal.ZERO, strategy.calculateDiscount(order, rule));
        }

        @Test
        @DisplayName("Trả về ZERO khi số lượng sản phẩm X chưa đủ điều kiện")
        void returns_zero_when_buy_quantity_not_enough() {
            RuleCampaign rule = RuleCampaign.builder().payload(buyXGetYPayload(1, 2, 3, 1)).build();

            Order order = orderWithItems(itemOf(1, 1, 200000), itemOf(3, 1, 50000));

            assertEquals(BigDecimal.ZERO, strategy.calculateDiscount(order, rule));
        }

        @Test
        @DisplayName("Trả về ZERO khi sản phẩm Y không có trong đơn hàng")
        void returns_zero_when_get_product_not_in_order() {
            RuleCampaign rule = RuleCampaign.builder().payload(buyXGetYPayload(1, 2, 3, 1)).build();

            Order order = orderWithItems(itemOf(1, 4, 200000));

            assertEquals(BigDecimal.ZERO, strategy.calculateDiscount(order, rule));
        }

        @Test
        @DisplayName("Throw MISSING_PAYLOAD khi thiếu x_product hoặc y_product")
        void throws_when_payload_missing_products() {
            RuleCampaign rule = RuleCampaign.builder().payload(new HashMap<>()).build();

            Order order = orderWithItems(itemOf(1, 4, 200000));

            AppException ex =
                    assertThrows(AppException.class, () -> strategy.calculateDiscount(order, rule));
            assertEquals(RuleCampaignErrorCode.MISSING_PAYLOAD, ex.getErrorCode());
        }

        @Test
        @DisplayName("Throw MISSING_PAYLOAD khi thiếu key bên trong x_product")
        void throws_when_product_payload_missing_keys() {
            Map<String, Object> xProduct = new HashMap<>();
            xProduct.put("product_id", 1);

            Map<String, Object> yProduct = new HashMap<>();
            yProduct.put("product_id", 3);
            yProduct.put("product_quantity", 1);

            Map<String, Object> payload = new HashMap<>();
            payload.put("x_product", xProduct);
            payload.put("y_product", yProduct);

            RuleCampaign rule = RuleCampaign.builder().payload(payload).build();
            Order order = orderWithItems(itemOf(1, 4, 200000));

            AppException ex =
                    assertThrows(AppException.class, () -> strategy.calculateDiscount(order, rule));
            assertEquals(RuleCampaignErrorCode.MISSING_PAYLOAD, ex.getErrorCode());
        }

        // helpers
        private Map<String, Object> buyXGetYPayload(int xId, int xQty, int yId, int yQty) {
            Map<String, Object> xProduct = new HashMap<>();
            xProduct.put("product_id", xId);
            xProduct.put("product_quantity", xQty);

            Map<String, Object> yProduct = new HashMap<>();
            yProduct.put("product_id", yId);
            yProduct.put("product_quantity", yQty);

            Map<String, Object> payload = new HashMap<>();
            payload.put("x_product", xProduct);
            payload.put("y_product", yProduct);
            return payload;
        }

        private OrderItem itemOf(int productId, int quantity, long price) {
            Product product = Product.builder().id(productId).build();
            return OrderItem.builder()
                    .product(product)
                    .quantity(quantity)
                    .price(BigDecimal.valueOf(price))
                    .build();
        }

        private Order orderWithItems(OrderItem... items) {
            return Order.builder()
                    .totalPrice(BigDecimal.valueOf(1000000))
                    .items(List.of(items))
                    .build();
        }
    }

    private Order orderOf(long amount) {
        return Order.builder().totalPrice(BigDecimal.valueOf(amount)).build();
    }
}
