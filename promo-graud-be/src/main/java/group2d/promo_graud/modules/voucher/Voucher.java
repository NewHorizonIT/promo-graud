package group2d.promo_graud.modules.voucher;

import java.time.LocalDateTime;

import group2d.promo_graud.modules.voucher.enums.DistributionChannel;
import group2d.promo_graud.modules.voucher.enums.VoucherStatus;
import group2d.promo_graud.modules.voucher.enums.VoucherType;
import jakarta.persistence.*;

import lombok.*;

import group2d.promo_graud.modules.rules.RuleCampaign;

@Builder
@Entity
@Table(name = "voucher")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "code", length = 50, nullable = false, unique = true)
    private String code;

    // Nếu type có các giá trị cố định, bạn có thể chuyển thành Enum (VD: VoucherTypeEnum)
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 10, nullable = false)
    private VoucherType type = VoucherType.GENERIC;

    // Quan hệ N-1 với bảng rule_campaign
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    private RuleCampaign ruleCampaign;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "quantity_remain", nullable = false)
    private Integer quantityRemain;

    @Builder.Default
    @Column(name = "limit_client", nullable = false)
    private Integer limitClient = 1;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "distribution_channel", length = 10, nullable = false)
    private DistributionChannel distributionChannel = DistributionChannel.WEBHOOK;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private VoucherStatus status = VoucherStatus.ACTIVE;
}
