package group2d.promo_graud.modules.products;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "name", length = 255, nullable = false)
  private String name;

  // Sử dụng columnDefinition = "TEXT" để ép kiểu chuẩn xác với PostgreSQL
  @Column(name = "images", columnDefinition = "TEXT")
  private String images;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "price", precision = 15, scale = 2, nullable = false)
  private BigDecimal price;

  // Quan hệ N-1 với bảng type_of_product
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "type_id")
  private TypeOfProduct typeOfProduct;

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
