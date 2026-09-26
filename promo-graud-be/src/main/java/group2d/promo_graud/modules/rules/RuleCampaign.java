package group2d.promo_graud.modules.rules;

import group2d.promo_graud.modules.campaigns.Campaign;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "rule_campaign")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleCampaign {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "name", length = 255, nullable = false)
  private String name;

  // Quan hệ N-1 với bảng Campaign
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "campaign_id", nullable = false)
  private Campaign campaign;

  @Column(name = "type_of_rule", length = 30, nullable = false)
  private String typeOfRule;

  @Column(name = "value", precision = 15, scale = 2)
  private BigDecimal value;

  @Column(name = "max_discount_value", precision = 15, scale = 2)
  private BigDecimal maxDiscountValue;

  @Column(name = "min_order_value", precision = 15, scale = 2)
  private BigDecimal minOrderValue;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(name = "type_of_user", length = 20, nullable = false)
  private RuleUserType typeOfUser = RuleUserType.ALL;

  @Column(name = "start_time")
  private LocalDateTime startTime;

  @Column(name = "end_time")
  private LocalDateTime endTime;

  // Hỗ trợ kiểu JSONB trong Hibernate 6
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "payload", columnDefinition = "jsonb")
  private Map<String, Object> payload;
  // Mẹo: Bạn có thể thay Map<String, Object> bằng một custom class (VD: PayloadDto) nếu JSON có cấu trúc cố định.

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(name = "status", length = 20, nullable = false)
  private RuleStatus status = RuleStatus.ACTIVE;

  @Builder.Default
  @Column(name = "is_deleted", nullable = false)
  private Boolean isDeleted = false;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;
}
