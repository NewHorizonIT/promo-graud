package group2d.promo_graud.modules.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import group2d.promo_graud.modules.user.entity.TypeOfUser;
import group2d.promo_graud.modules.user.enums.UserTypeEnum;

@Repository
public interface TypeOfUserRepository extends JpaRepository<TypeOfUser, Integer> {
    Optional<TypeOfUser> findByType(UserTypeEnum type);
}
