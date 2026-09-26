package group2d.promo_graud.modules.voucher;

import group2d.promo_graud.modules.orders.Order;
import group2d.promo_graud.modules.orders.OrderStatus;
import group2d.promo_graud.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "voucher_redemption")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherRedemption {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  // Quan hệ N-1 với bảng "user"
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  // Quan hệ N-1 với bảng "order"
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  // Quan hệ N-1 với bảng voucher
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "voucher_id", nullable = false)
  private Voucher voucher;

  @Column(name = "ip_address", length = 45, nullable = false)
  private String ipAddress;

  @Column(name = "device_fingerprint", length = 255)
  private String deviceFingerprint;

  // Tái sử dụng lại Enum OrderStatus đã tạo ở bảng Order
  @Enumerated(EnumType.STRING)
  @Column(name = "status_of_order", length = 20, nullable = false)
  private OrderStatus statusOfOrder;

  // Định nghĩa kiểu TEXT cho cột reason
  @Column(name = "reason", columnDefinition = "TEXT")
  private String reason;
}
