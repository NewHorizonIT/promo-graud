package group2d.promo_graud.modules.voucher.dto.responses;

import java.time.LocalDateTime;

import lombok.*;
import lombok.experimental.FieldDefaults;

import group2d.promo_graud.modules.voucher.enums.DistributionChannel;
import group2d.promo_graud.modules.voucher.enums.DistributionLogStatus;
import group2d.promo_graud.modules.voucher.enums.VoucherType;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class DistributionResponse {
    private Integer id;
    private Integer voucherId;
    private String voucherCode;
    private VoucherType type;
    private DistributionChannel channel;
    private String recipient;
    private DistributionLogStatus status;
    private Integer retryCount;
    private LocalDateTime lastAttemptAt;
    private String errorMessage;
}
