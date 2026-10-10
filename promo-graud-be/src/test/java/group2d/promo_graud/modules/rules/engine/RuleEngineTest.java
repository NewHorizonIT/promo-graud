package group2d.promo_graud.modules.rules.engine;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import group2d.promo_graud.modules.orders.Order;
import group2d.promo_graud.modules.rules.RuleCampaign;
import group2d.promo_graud.modules.rules.engine.strategy.RuleStrategy;
import group2d.promo_graud.modules.rules.enums.RuleCampaignErrorCode;
import group2d.promo_graud.modules.rules.enums.TypeOfRule;
import group2d.promo_graud.modules.user.entity.User;
import group2d.promo_graud.shared.exception.AppException;

@ExtendWith(MockitoExtension.class)
@DisplayName("RuleEngine")
class RuleEngineTest {

    @Mock private EligibilityChecker eligibilityChecker;
    @Mock private StrategyFactory strategyFactory;
    @InjectMocks private RuleEngine ruleEngine;

    private final Order order = Order.builder().totalPrice(BigDecimal.valueOf(1_000_000)).build();

    private final RuleCampaign rule =
            RuleCampaign.builder().typeOfRule(TypeOfRule.PERCENTAGE).build();

    private final User user = User.builder().build();

    @Test
    @DisplayName("Trả về discount khi user đủ điều kiện")
    void apply_success_returns_discount() {
        RuleStrategy mockStrategy = mock(RuleStrategy.class);
        when(eligibilityChecker.isEligible(order, rule, user)).thenReturn(true);
        when(strategyFactory.resolveStrategy(TypeOfRule.PERCENTAGE)).thenReturn(mockStrategy);
        when(mockStrategy.calculateDiscount(order, rule)).thenReturn(BigDecimal.valueOf(100_000));

        BigDecimal result = ruleEngine.applyRules(order, rule, user);

        assertEquals(BigDecimal.valueOf(100_000), result);
        verify(strategyFactory).resolveStrategy(TypeOfRule.PERCENTAGE);
        verify(mockStrategy).calculateDiscount(order, rule);
    }

    @Test
    @DisplayName("Throw RULE_NOT_ELIGIBLE khi user không đủ điều kiện")
    void apply_throws_when_not_eligible() {
        when(eligibilityChecker.isEligible(order, rule, user)).thenReturn(false);

        AppException ex =
                assertThrows(AppException.class, () -> ruleEngine.applyRules(order, rule, user));

        assertEquals(RuleCampaignErrorCode.RULE_NOT_ELIGIBLE, ex.getErrorCode());
        verifyNoInteractions(strategyFactory);
    }
}
