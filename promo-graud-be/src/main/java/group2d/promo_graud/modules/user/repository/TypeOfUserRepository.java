package group2d.promo_graud.modules.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import group2d.promo_graud.modules.user.entity.TypeOfUser;

@Repository
public interface TypeOfUserRepository extends JpaRepository<TypeOfUser, Integer> {}
