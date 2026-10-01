package group2d.promo_graud.modules.products.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import group2d.promo_graud.modules.products.entity.TypeOfProduct;

@Repository
public interface TypeOfProductRepository extends JpaRepository<TypeOfProduct, Integer> {}
