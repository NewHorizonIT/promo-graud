package group2d.promo_graud.modules.voucher;

import group2d.promo_graud.modules.rules.RuleCampaign;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "voucher")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voucher {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "code", length = 50, nullable = false, unique = true)
  private String code;

  // Nếu type có các giá trị cố định, bạn có thể chuyển thành Enum (VD: VoucherTypeEnum)
  @Column(name = "type", length = 10, nullable = false)
  private String type;

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

  @Column(name = "expired_at", nullable = false)
  private LocalDateTime expiredAt;

  // Nếu channel có các giá trị cố định (EMAIL, SMS...), bạn nên chuyển thành Enum
  @Column(name = "distribution_channel", length = 10, nullable = false)
  private String distributionChannel;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(name = "status", length = 20, nullable = false)
  private VoucherStatus status = VoucherStatus.ACTIVE;
}
