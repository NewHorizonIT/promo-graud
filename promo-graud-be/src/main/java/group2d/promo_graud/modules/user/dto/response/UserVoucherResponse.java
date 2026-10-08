package group2d.promo_graud.modules.user.dto.response;

import java.time.LocalDateTime;

import lombok.*;
import lombok.experimental.FieldDefaults;

import group2d.promo_graud.modules.user.entity.User;
import group2d.promo_graud.modules.user.enums.UserVoucherStatus;
import group2d.promo_graud.modules.voucher.Voucher;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class UserVoucherResponse {
    Integer id;
    User user;
    Voucher voucher;
    LocalDateTime collectedAt;
    UserVoucherStatus status;
}
