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
    boolean existsByUsername(String username);

    Optional<User> findByIdAndIsDeletedFalse(Integer id);

    public Optional<User> findByUsername(String username);

    @Query(
            "SELECT u FROM User u "
                    + "WHERE u.isDeleted = false "
                    + "AND (:userTypeId IS NULL OR u.typeOfUser.id = :userTypeId) "
                    + "AND (:name = '' OR LOWER(u.username) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<User> getAllUser(
            @Param("userTypeId") Integer userTypeId, @Param("name") String name, Pageable pageable);

    List<User> findByIsDeletedFalse();

    List<User> findByTypeOfUserTypeAndIsDeletedFalse(UserTypeEnum type);

    List<User> findByIdInAndIsDeletedFalse(List<Integer> ids);
}
