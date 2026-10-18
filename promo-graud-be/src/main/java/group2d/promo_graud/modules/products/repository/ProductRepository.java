package group2d.promo_graud.modules.products.repository;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import group2d.promo_graud.modules.products.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    Page<Product> findAllByIsDeletedFalse(Pageable pageable);

    Optional<Product> findByIdAndIsDeletedFalse(Integer id);

    @Query(
            "SELECT p FROM Product p "
                    + "WHERE p.isDeleted = false "
                    + "AND (:typeId IS NULL OR p.typeOfProduct.id = :typeId) "
                    + "AND (:minPrice IS NULL OR p.price >= :minPrice) "
                    + "AND (:maxPrice IS NULL OR p.price <= :maxPrice) "
                    + "AND (:name = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<Product> getAll(
            @Param("typeId") Integer typeId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("name") String name,
            Pageable pageable);
}
