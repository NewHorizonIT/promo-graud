package group2d.promo_graud.modules.rules.engine;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import group2d.promo_graud.modules.orders.Order;
import group2d.promo_graud.modules.rules.RuleCampaign;
import group2d.promo_graud.modules.rules.enums.RuleStatus;
import group2d.promo_graud.modules.user.entity.TypeOfUser;
import group2d.promo_graud.modules.user.entity.User;
import group2d.promo_graud.modules.user.enums.UserTypeEnum;

@DisplayName("EligibilityChecker")
class EligibilityCheckerTest {

    private final EligibilityChecker checker = new EligibilityChecker();

    // ─── helpers ─────────────────────────────────────────────────────────────

    private RuleCampaign baseRule() {
        return RuleCampaign.builder()
                .status(RuleStatus.ACTIVE)
                .isDeleted(false)
                .typeOfUser(UserTypeEnum.NORMAL)
                .startTime(LocalDateTime.now().minusDays(1))
                .endTime(LocalDateTime.now().plusDays(1))
                .build();
    }

    private Order orderOf(long amount) {
        return Order.builder().totalPrice(BigDecimal.valueOf(amount)).build();
    }

    private User userOfType(UserTypeEnum type) {
        TypeOfUser typeOfUser = TypeOfUser.builder().type(type).build();
        return User.builder().typeOfUser(typeOfUser).build();
    }

    // ─── checkStatus ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("checkStatus")
    class CheckStatus {

        @Test
        @DisplayName("Eligible khi rule ACTIVE và chưa xóa")
        void eligible_active_not_deleted() {
            assertTrue(
                    checker.isEligible(
                            orderOf(500_000), baseRule(), userOfType(UserTypeEnum.NORMAL)));
        }

        @Test
        @DisplayName("Không eligible khi rule INACTIVE")
        void not_eligible_inactive() {
            RuleCampaign rule = baseRule();
            rule.setStatus(RuleStatus.INACTIVE);
            assertFalse(
                    checker.isEligible(orderOf(500_000), rule, userOfType(UserTypeEnum.NORMAL)));
        }

        @Test
        @DisplayName("Không eligible khi rule đã bị soft-delete")
        void not_eligible_deleted() {
            RuleCampaign rule = baseRule();
            rule.setIsDeleted(true);
            assertFalse(
                    checker.isEligible(orderOf(500_000), rule, userOfType(UserTypeEnum.NORMAL)));
        }
    }

    // ─── checkTimeWindow ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("checkTimeWindow")
    class CheckTimeWindow {

        @Test
        @DisplayName("Eligible khi đang trong khung giờ hợp lệ")
        void eligible_within_window() {
            assertTrue(
                    checker.isEligible(
                            orderOf(500_000), baseRule(), userOfType(UserTypeEnum.NORMAL)));
        }

        @Test
        @DisplayName("Không eligible khi chưa đến startTime")
        void not_eligible_before_start() {
            RuleCampaign rule = baseRule();
            rule.setStartTime(LocalDateTime.now().plusDays(1));
            assertFalse(
                    checker.isEligible(orderOf(500_000), rule, userOfType(UserTypeEnum.NORMAL)));
        }

        @Test
        @DisplayName("Không eligible khi đã qua endTime")
        void not_eligible_after_end() {
            RuleCampaign rule = baseRule();
            rule.setEndTime(LocalDateTime.now().minusDays(1));
            assertFalse(
                    checker.isEligible(orderOf(500_000), rule, userOfType(UserTypeEnum.NORMAL)));
        }

        @Test
        @DisplayName("Eligible khi startTime và endTime null — không giới hạn thời gian")
        void eligible_null_time_window() {
            RuleCampaign rule = baseRule();
            rule.setStartTime(null);
            rule.setEndTime(null);
            assertTrue(checker.isEligible(orderOf(500_000), rule, userOfType(UserTypeEnum.NORMAL)));
        }
    }

    // ─── checkUserType ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("checkUserType")
    class CheckUserType {

        @Test
        @DisplayName("Eligible khi rule áp dụng cho ALL user")
        void eligible_rule_targets_all() {
            assertTrue(
                    checker.isEligible(
                            orderOf(500_000), baseRule(), userOfType(UserTypeEnum.BRONZE)));
        }

        @Test
        @DisplayName("Eligible khi user đúng hạng mà rule yêu cầu")
        void eligible_matching_user_type() {
            RuleCampaign rule = baseRule();
            rule.setTypeOfUser(UserTypeEnum.GOLD);
            assertTrue(checker.isEligible(orderOf(500_000), rule, userOfType(UserTypeEnum.GOLD)));
        }

        @Test
        @DisplayName("Không eligible khi user sai hạng")
        void not_eligible_wrong_user_type() {
            RuleCampaign rule = baseRule();
            rule.setTypeOfUser(UserTypeEnum.GOLD);
            assertFalse(
                    checker.isEligible(orderOf(500_000), rule, userOfType(UserTypeEnum.NORMAL)));
        }

        @Test
        @DisplayName("Không eligible khi user chưa có typeOfUser (null)")
        void not_eligible_null_type_of_user() {
            RuleCampaign rule = baseRule();
            rule.setTypeOfUser(UserTypeEnum.GOLD);
            User user = User.builder().typeOfUser(null).build();
            assertFalse(checker.isEligible(orderOf(500_000), rule, user));
        }

        @Test
        @DisplayName("Không eligible khi typeOfUser.getType() là null")
        void not_eligible_null_type_inside_type_of_user() {
            RuleCampaign rule = baseRule();
            rule.setTypeOfUser(UserTypeEnum.GOLD);
            TypeOfUser typeOfUser = TypeOfUser.builder().type(null).build();
            User user = User.builder().typeOfUser(typeOfUser).build();
            assertFalse(checker.isEligible(orderOf(500_000), rule, user));
        }
    }

    // ─── checkMinOrder ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("checkMinOrder")
    class CheckMinOrder {

        @Test
        @DisplayName("Eligible khi không có minOrderValue")
        void eligible_no_min_order() {
            RuleCampaign rule = baseRule(); // minOrderValue = null
            assertTrue(checker.isEligible(orderOf(100_000), rule, userOfType(UserTypeEnum.NORMAL)));
        }

        @Test
        @DisplayName("Eligible khi đơn hàng đúng bằng minOrderValue — biên dưới")
        void eligible_order_equals_min() {
            RuleCampaign rule = baseRule();
            rule.setMinOrderValue(BigDecimal.valueOf(500_000));
            assertTrue(checker.isEligible(orderOf(500_000), rule, userOfType(UserTypeEnum.NORMAL)));
        }

        @Test
        @DisplayName("Eligible khi đơn hàng lớn hơn minOrderValue")
        void eligible_order_above_min() {
            RuleCampaign rule = baseRule();
            rule.setMinOrderValue(BigDecimal.valueOf(500_000));
            assertTrue(
                    checker.isEligible(orderOf(1_000_000), rule, userOfType(UserTypeEnum.NORMAL)));
        }

        @Test
        @DisplayName("Không eligible khi đơn hàng nhỏ hơn minOrderValue")
        void not_eligible_order_below_min() {
            RuleCampaign rule = baseRule();
            rule.setMinOrderValue(BigDecimal.valueOf(500_000));
            assertFalse(
                    checker.isEligible(orderOf(200_000), rule, userOfType(UserTypeEnum.NORMAL)));
        }
    }
}
