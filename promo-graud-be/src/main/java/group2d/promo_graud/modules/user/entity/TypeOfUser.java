package group2d.promo_graud.modules.user.entity;

import java.math.BigDecimal;

import jakarta.persistence.*;

import lombok.*;

import group2d.promo_graud.modules.user.enums.UserTypeEnum;

@Entity
@Table(name = "type_of_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TypeOfUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20, nullable = false)
    private UserTypeEnum type;

    @Column(name = "threshold", precision = 15, scale = 2, nullable = false)
    private BigDecimal threshold;
}
