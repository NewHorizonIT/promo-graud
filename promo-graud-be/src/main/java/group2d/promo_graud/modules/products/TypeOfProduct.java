package group2d.promo_graud.modules.products;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "type_of_product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TypeOfProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "type", length = 100, nullable = false)
    private String type;
}
