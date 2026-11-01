package group2d.promo_graud.modules.user.dto.request;

import jakarta.validation.constraints.NotNull;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserVoucherRequest {

    Integer userId;

    @NotNull(message = "voucher_id không được null")
    Integer voucherId;
}
