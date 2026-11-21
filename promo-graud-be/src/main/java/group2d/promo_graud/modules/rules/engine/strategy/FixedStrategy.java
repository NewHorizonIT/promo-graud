package group2d.promo_graud.modules.rules.engine.strategy;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import group2d.promo_graud.modules.orders.entity.Order;
import group2d.promo_graud.modules.rules.RuleCampaign;
import group2d.promo_graud.modules.rules.enums.TypeOfRule;

@Component
public class FixedStrategy implements RuleStrategy {
    // Tính tiền giảm giá cố định theo giá trị của rule
    // lấy giá trị giảm từ rule -> so sánh với tổng tiền đơn -> trả về số nhỏ hơn để không giảm quá
    // tổng đơn
    @Override
    public BigDecimal calculateDiscount(Order order, RuleCampaign rule) {
        return rule.getValue().min(order.getTotalPrice());
    }

    @Override
    public TypeOfRule getRuleType() {
        return TypeOfRule.FIXED_AMOUNT;
    }
}
