package group2d.promo_graud.modules.fraud;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_log")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "user_id")
  private Integer userId;

  @Column(name = "ip_address", nullable = false)
  private String ipAddress;

  @Column(name = "device_fingerprint")
  private String deviceFingerprint;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private FraudReason reason;

  @Builder.Default
  @Column(name = "blocked_at", nullable = false)
  private LocalDateTime blockedAt = LocalDateTime.now();
}
