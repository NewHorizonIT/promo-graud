package group2d.promo_graud.modules.rules.engine.strategy;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import group2d.promo_graud.modules.orders.Order;
import group2d.promo_graud.modules.rules.RuleCampaign;
import group2d.promo_graud.modules.rules.enums.TypeOfRule;

@Component
public class FixedStrategy implements RuleStrategy {

    @Override
    public BigDecimal calculateDiscount(Order order, RuleCampaign rule) {
        return rule.getValue().min(order.getTotalPrice());
    }

    @Override
    public TypeOfRule getRuleType() {
        return TypeOfRule.FIXED_AMOUNT;
    }
}
