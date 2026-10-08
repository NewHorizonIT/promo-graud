package group2d.promo_graud.modules.user.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import group2d.promo_graud.modules.user.enums.UserVoucherStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserVoucherRequest {

    @NotNull(message = "user_id không được null")
    Integer userId;

    @NotNull(message = "voucher_id không được null")
    Integer voucherId;

    LocalDateTime collectedAt;

    UserVoucherStatus status;
}
