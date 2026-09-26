package group2d.promo_graud.modules.user.entity;

import group2d.promo_graud.modules.user.enums.RoleEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "\"user\"") // Escape từ khóa hệ thống "user" trong PostgreSQL
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "username", length = 100, nullable = false, unique = true)
  private String username;

  @Column(name = "email", length = 255, nullable = false, unique = true)
  private String email;

  @Column(name = "password", length = 255, nullable = false)
  private String password;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(name = "role", length = 10, nullable = false)
  private RoleEnum role = RoleEnum.USER;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "type_id")
  private TypeOfUser typeOfUser;

  @Builder.Default
  @Column(name = "is_deleted", nullable = false)
  private Boolean isDeleted = false;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}
