package group2d.promo_graud.modules.migrateTest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

import group2d.promo_graud.modules.user.entity.User;
import group2d.promo_graud.modules.user.entity.UserVoucher;
import group2d.promo_graud.modules.user.enums.RoleEnum;
import group2d.promo_graud.modules.user.enums.UserVoucherStatus;
import group2d.promo_graud.modules.user.repository.UserRepository;
import group2d.promo_graud.modules.user.repository.UserVoucherRepository;
import group2d.promo_graud.modules.voucher.Voucher;
import group2d.promo_graud.modules.voucher.VoucherRepository;
import group2d.promo_graud.modules.voucher.enums.VoucherStatus;
import group2d.promo_graud.modules.voucher.enums.VoucherType;

@SpringBootTest
class SeedK6UserVouchersTest {

    @Autowired private UserRepository userRepository;

    @Autowired private UserVoucherRepository userVoucherRepository;

    @Autowired private VoucherRepository voucherRepository;

    @Test
    void seedUserVouchersForK6() {
        final int targetCount = 500;
        final int voucherId = 2;
        final int availableQuantity = 50;

        // 1. Lấy đúng 500 tài khoản K6, không lấy user thông thường/admin.
        List<User> users =
                userRepository.findAll().stream()
                        .filter(user -> user.getUsername().startsWith("k6_user_"))
                        .filter(user -> !Boolean.TRUE.equals(user.getIsDeleted()))
                        .filter(user -> user.getRole() == RoleEnum.USER)
                        .sorted(Comparator.comparing(User::getId))
                        .limit(targetCount)
                        .toList();

        assertEquals(targetCount, users.size(), "Phải có đủ 500 tài khoản k6_user_ trước khi seed");

        // 2. Kiểm tra voucher.
        Voucher voucher =
                voucherRepository
                        .findById(voucherId)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Voucher không tồn tại: " + voucherId));

        assertEquals(VoucherType.GENERIC, voucher.getType());
        assertEquals(VoucherStatus.ACTIVE, voucher.getStatus());
        assertTrue(
                voucher.getQuantity() >= availableQuantity, "Quantity phải >= số lượt muốn test");

        // Reset số lượt còn lại để test có kết quả xác định.
        voucher.setQuantityRemain(availableQuantity);
        voucherRepository.save(voucher);

        // 3. Tạo UserVoucher cho cả 500 user.
        List<UserVoucher> records = new ArrayList<>();

        for (User user : users) {
            UserVoucher userVoucher =
                    userVoucherRepository
                            .findByUserIdAndVoucherId(user.getId(), voucherId)
                            .orElseGet(
                                    () ->
                                            UserVoucher.builder()
                                                    .user(user)
                                                    .voucher(voucher)
                                                    .build());

            // Chỉ reset trạng thái trong database test.
            userVoucher.setStatus(UserVoucherStatus.UNUSED);
            records.add(userVoucher);
        }

        userVoucherRepository.saveAll(records);

        System.out.println("========== K6 SEED RESULT ==========");
        System.out.println("Users: " + users.size());
        System.out.println("Voucher ID: " + voucherId);
        System.out.println("Available quantity: " + voucher.getQuantityRemain());
        System.out.println("UserVoucher records: " + records.size());
        System.out.println("====================================");
    }
}
