package group2d.promo_graud.modules.voucher;

import group2d.promo_graud.modules.orders.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;

@Repository
public interface VoucherRedemptionRepository extends JpaRepository<VoucherRedemption, Integer> {
  // Số lần user đã dùng voucher này
  @Query(
    "SELECT COUNT(r) FROM VoucherRedemption r "
      + "WHERE r.user.id = :userId AND r.voucher.id = :voucherId "
      + "AND r.statusOfOrder IN :statuses")
  long countByUser(
    @Param("userId") Integer userId,
    @Param("voucherId") Integer voucherId,
    @Param("statuses") Collection<OrderStatus> statuses);

  // Số lần thiết bị này đã dùng voucher này (kể cả bằng tài khoản khác)
  @Query(
    "SELECT COUNT(r) FROM VoucherRedemption r "
      + "WHERE r.voucher.id = :voucherId AND r.deviceFingerprint = :fp "
      + "AND r.statusOfOrder IN :statuses")
  long countByDevice(
    @Param("voucherId") Integer voucherId,
    @Param("fp") String fingerprint,
    @Param("statuses") Collection<OrderStatus> statuses);

  // Số tài khoản khác nhau trên cùng thiết bị kể từ mốc thời gian
  @Query(
    "SELECT COUNT(DISTINCT r.user.id) FROM VoucherRedemption r "
      + "WHERE r.deviceFingerprint = :fp AND r.user.id <> :userId "
      + "AND r.order.createdAt > :since")
  long countOtherUsersOnDevice(
    @Param("fp") String fingerprint,
    @Param("userId") Integer userId,
    @Param("since") LocalDateTime since);

  // Số lần redeem từ một IP kể từ mốc thời gian
  @Query(
    "SELECT COUNT(r) FROM VoucherRedemption r "
      + "WHERE r.ipAddress = :ip AND r.statusOfOrder IN :statuses "
      + "AND r.order.createdAt > :since")
  long countByIpSince(
    @Param("ip") String ip,
    @Param("statuses") Collection<OrderStatus> statuses,
    @Param("since") LocalDateTime since);
}
