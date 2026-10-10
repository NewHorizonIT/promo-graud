package group2d.promo_graud.modules.rules.engine.strategy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import group2d.promo_graud.modules.orders.Order;
import group2d.promo_graud.modules.orders.OrderItem;
import group2d.promo_graud.modules.rules.RuleCampaign;
import group2d.promo_graud.modules.rules.enums.RuleCampaignErrorCode;
import group2d.promo_graud.modules.rules.enums.TypeOfRule;
import group2d.promo_graud.shared.exception.AppException;

@Component
public class BuyXGetYStrategy implements RuleStrategy {

    @Override
    @SuppressWarnings("unchecked")
    public BigDecimal calculateDiscount(Order order, RuleCampaign rule) {
        Map<String, Object> payload = rule.getPayload();

        Map<String, Object> xProduct = (Map<String, Object>) payload.get("x_product");
        Map<String, Object> yProduct = (Map<String, Object>) payload.get("y_product");

        if (xProduct == null || yProduct == null) {
            throw new AppException(RuleCampaignErrorCode.MISSING_PAYLOAD);
        }

        Number xProductId = (Number) xProduct.get("product_id");
        Number xProductQuantity = (Number) xProduct.get("product_quantity");
        Number yProductId = (Number) yProduct.get("product_id");
        Number yProductQuantity = (Number) yProduct.get("product_quantity");
        if (xProductId == null
                || xProductQuantity == null
                || yProductId == null
                || yProductQuantity == null) {
            throw new AppException(RuleCampaignErrorCode.MISSING_PAYLOAD);
        }

        Integer xId = xProductId.intValue();
        Integer xQuantity = xProductQuantity.intValue();
        Integer yId = yProductId.intValue();
        Integer yQuantity = yProductQuantity.intValue();

        List<OrderItem> orderItems = order.getItems();

        OrderItem productBuy =
                orderItems.stream()
                        .filter(item -> item.getProduct().getId().equals(xId))
                        .findFirst()
                        .orElse(null);

        if (productBuy == null || productBuy.getQuantity() < xQuantity) {
            return BigDecimal.ZERO;
        }

        int sets = productBuy.getQuantity() / xQuantity;
        int totalFree = sets * yQuantity;

        OrderItem yInOrderItems =
                orderItems.stream()
                        .filter(item -> item.getProduct().getId().equals(yId))
                        .findFirst()
                        .orElse(null);

        if (yInOrderItems == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal result = yInOrderItems.getPrice().multiply(new BigDecimal(totalFree));

        return result;
    }

    @Override
    public TypeOfRule getRuleType() {
        return TypeOfRule.BUY_X_GET_Y;
    }
}
