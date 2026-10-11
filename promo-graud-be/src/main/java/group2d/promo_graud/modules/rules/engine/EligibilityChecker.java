package group2d.promo_graud.modules.rules.engine;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import group2d.promo_graud.modules.orders.Order;
import group2d.promo_graud.modules.rules.RuleCampaign;
import group2d.promo_graud.modules.rules.enums.RuleStatus;
import group2d.promo_graud.modules.user.entity.User;
import group2d.promo_graud.modules.user.enums.UserTypeEnum;

@Component
public class EligibilityChecker {

    public boolean isEligible(Order order, RuleCampaign rule, User user) {
        return checkStatus(rule)
                && checkTimeWindow(rule)
                && checkUserType(rule, user)
                && checkMinOrder(rule, order);
    }

    private boolean checkStatus(RuleCampaign rule) {
        return RuleStatus.ACTIVE.equals(rule.getStatus()) && !rule.getIsDeleted();
    }

    private boolean checkTimeWindow(RuleCampaign rule) {
        LocalDateTime now = LocalDateTime.now();
        if (rule.getStartTime() != null && now.isBefore(rule.getStartTime())) {
            return false;
        }
        if (rule.getEndTime() != null && now.isAfter(rule.getEndTime())) {
            return false;
        }
        return true;
    }

    private boolean checkUserType(RuleCampaign rule, User user) {
        if (UserTypeEnum.NORMAL.equals(rule.getTypeOfUser())) {
            return true;
        }
        if (user.getTypeOfUser() == null || user.getTypeOfUser().getType() == null) {
            return false;
        }
        return rule.getTypeOfUser().equals(user.getTypeOfUser().getType());
    }

    private boolean checkMinOrder(RuleCampaign rule, Order order) {
        if (rule.getMinOrderValue() == null) {
            return true;
        }
        return order.getTotalPrice().compareTo(rule.getMinOrderValue()) >= 0;
    }
}
