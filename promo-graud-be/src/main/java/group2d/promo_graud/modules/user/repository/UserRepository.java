package group2d.promo_graud.modules.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import group2d.promo_graud.modules.user.entity.User;
import group2d.promo_graud.modules.user.enums.UserTypeEnum;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    public boolean existsByUsername(String username);

    public Optional<User> findByUsername(String username);

    @Query(
            "SELECT u FROM User u "
                    + "LEFT JOIN u.typeOfUser t "
                    + "WHERE u.isDeleted = false "
                    + "AND LOWER(u.username) LIKE LOWER(CONCAT('%', COALESCE(:userName, ''), '%')) "
                    + "AND (:typeOfUser IS NULL OR t.type = :typeOfUser)")
    Page<User> getAll(
            @Param("userName") String userName,
            @Param("typeOfUser") UserTypeEnum typeOfUser,
            Pageable pageable);

    @Query("SELECT u.id FROM User u WHERE u.typeOfUser.type = :userType")
    List<Integer> findIdsByType(@Param("userType") UserTypeEnum userType);
}
