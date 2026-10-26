package group2d.promo_graud.modules.voucher.dto.requests;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

import group2d.promo_graud.modules.user.enums.UserTypeEnum;
import group2d.promo_graud.modules.voucher.enums.DistributionChannel;
import group2d.promo_graud.modules.voucher.enums.VoucherType;

@Data
public class VoucherRequest {

    @NotNull private VoucherType type;

    private Integer ruleId;

    /** Nullable khi có targetUserType hoặc targetUserIds — BE tự tính quantity từ số user. */
    @Min(1)
    private Integer quantity;

    @NotNull private String prefix;

    @Min(1)
    private Integer limitClient = 1;

    @NotNull private DistributionChannel distributionChannel;

    /** Chỉ áp dụng với UNIQUE. null = không chỉ định loại user. */
    private UserTypeEnum targetUserType;

    /** Chỉ áp dụng với UNIQUE. null = không chỉ định danh sách user. */
    private List<Integer> targetUserIds;
}
