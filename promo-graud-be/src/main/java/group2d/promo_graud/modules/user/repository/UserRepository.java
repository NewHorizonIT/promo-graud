package group2d.promo_graud.modules.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import group2d.promo_graud.modules.user.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    public boolean existsByUsername(String username);

    public Optional<User> findByUsername(String username);
}
