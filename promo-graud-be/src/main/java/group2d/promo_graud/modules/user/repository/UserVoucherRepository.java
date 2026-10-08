package group2d.promo_graud.modules.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import group2d.promo_graud.modules.user.entity.UserVoucher;

@Repository
public interface UserVoucherRepository extends JpaRepository<UserVoucher, Integer> {
    public Optional<UserVoucher> findByUserIdAndVoucherId(Integer userId, Integer voucherId);
}
