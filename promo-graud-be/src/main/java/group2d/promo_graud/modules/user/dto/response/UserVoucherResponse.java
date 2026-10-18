package group2d.promo_graud.modules.user.dto.response;

import java.time.LocalDateTime;

import lombok.*;
import lombok.experimental.FieldDefaults;

import group2d.promo_graud.modules.user.enums.UserVoucherStatus;
import group2d.promo_graud.modules.voucher.dto.responses.VoucherResponse;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class UserVoucherResponse {
    Integer id;
    Integer userId;
    VoucherResponse voucher;
    LocalDateTime collectedAt;
    UserVoucherStatus status;
}
