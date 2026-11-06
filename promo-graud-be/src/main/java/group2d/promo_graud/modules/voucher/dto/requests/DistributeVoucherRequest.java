package group2d.promo_graud.modules.voucher.dto.requests;

import java.util.List;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import group2d.promo_graud.modules.user.enums.UserTypeEnum;
import group2d.promo_graud.modules.voucher.enums.DistributionChannel;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistributeVoucherRequest {
    @NotNull(message = "Rule ID không được để trống")
    private Integer ruleId;

    @NotNull(message = "Kênh phân phối không được để trống")
    private DistributionChannel distributionChannel;

    // CÁCH 1: Nếu Admin tick chọn từng người trên màn hình
    private List<Integer> userIds;

    // CÁCH 2: Nếu Admin bấm "Chọn tất cả người dùng hạng ..."
    private UserTypeEnum targetUserType;
    // true  -> chọn tất cả user (theo targetUserType nếu có), trừ excludedUserIds
    // false/null -> chỉ dùng userIds
    private Boolean selectAll;

    // Những user bị bỏ tick khi đang ở chế độ selectAll
    private List<Integer> excludedUserIds;
}
