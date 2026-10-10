package group2d.promo_graud.modules.rules.engine.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import group2d.promo_graud.modules.orders.Order;
import group2d.promo_graud.modules.rules.RuleCampaign;
import group2d.promo_graud.modules.rules.enums.TypeOfRule;

@Component
public class PercentageStrategy implements RuleStrategy {

    @Override
    public BigDecimal calculateDiscount(Order order, RuleCampaign rule) {
        BigDecimal discount =
                order.getTotalPrice()
                        .multiply(rule.getValue())
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        if (rule.getMaxDiscountValue() != null) {
            discount = discount.min(rule.getMaxDiscountValue());
        }

        return discount;
    }

    @Override
    public TypeOfRule getRuleType() {
        return TypeOfRule.PERCENTAGE;
    }
}
