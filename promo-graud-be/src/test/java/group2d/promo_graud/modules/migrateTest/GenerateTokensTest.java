package group2d.promo_graud.modules.migrateTest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.fasterxml.jackson.databind.ObjectMapper;

import group2d.promo_graud.modules.auth.AuthenticationService;
import group2d.promo_graud.modules.user.entity.TypeOfUser;
import group2d.promo_graud.modules.user.entity.User;
import group2d.promo_graud.modules.user.entity.UserVoucher;
import group2d.promo_graud.modules.user.enums.RoleEnum;
import group2d.promo_graud.modules.user.enums.UserTypeEnum;
import group2d.promo_graud.modules.user.enums.UserVoucherStatus;
import group2d.promo_graud.modules.user.repository.TypeOfUserRepository;
import group2d.promo_graud.modules.user.repository.UserRepository;
import group2d.promo_graud.modules.user.repository.UserVoucherRepository;
import group2d.promo_graud.modules.voucher.Voucher;
import group2d.promo_graud.modules.voucher.VoucherRepository;

@SpringBootTest
class GenerateTokensTest {

    @Autowired private UserRepository userRepository;

    @Autowired private TypeOfUserRepository typeOfUserRepository;

    @Autowired private VoucherRepository voucherRepository;

    @Autowired private UserVoucherRepository userVoucherRepository;

    @Autowired private AuthenticationService authenticationService;

    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void generate500TokensForK6() throws IOException {
        int targetCount = 500;
        Integer targetVoucherId = 2; // ID Voucher đang test performance

        // 1. Tạo đủ 500 user test (k6_user_1 -> k6_user_500) nếu chưa có
        TypeOfUser defaultType = typeOfUserRepository.findByType(UserTypeEnum.NORMAL).orElse(null);
        String defaultPassword = passwordEncoder.encode("123456");

        for (int i = 1; i <= targetCount; i++) {
            String username = "k6_user_" + i;
            String email = "k6_user_" + i + "@test.com";

            if (!userRepository.existsByUsername(username)) {
                User user =
                        User.builder()
                                .username(username)
                                .email(email)
                                .password(defaultPassword)
                                .role(RoleEnum.USER)
                                .typeOfUser(defaultType)
                                .isDeleted(false)
                                .build();
                userRepository.save(user);
            }
        }

        // 2. Lọc đúng 500 tài khoản k6_user_ và SẮP XẾP TĂNG DẦN THEO ID (đảm bảo VU 1 = k6_user_1)
        List<User> k6Users =
                userRepository.findAll().stream()
                        .filter(
                                u ->
                                        u.getUsername() != null
                                                && u.getUsername().startsWith("k6_user_"))
                        .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()))
                        .sorted(Comparator.comparing(User::getId))
                        .limit(targetCount)
                        .toList();

        if (k6Users.size() < targetCount) {
            throw new IllegalStateException(
                    "Không đủ 500 k6_user trong Database! Hiện có: " + k6Users.size());
        }

        // 3. Tự động SEED dữ liệu UserVoucher (Gán Voucher ID = 2 cho cả 500 user với trạng thái
        // UNUSED)
        Voucher voucher = voucherRepository.findById(targetVoucherId).orElse(null);
        if (voucher != null) {
            List<UserVoucher> newUserVouchers = new ArrayList<>();
            for (User user : k6Users) {
                boolean hasVoucher =
                        userVoucherRepository
                                .findByUserIdAndVoucherId(user.getId(), voucher.getId())
                                .isPresent();
                if (!hasVoucher) {
                    newUserVouchers.add(
                            UserVoucher.builder()
                                    .user(user)
                                    .voucher(voucher)
                                    .status(UserVoucherStatus.UNUSED)
                                    .build());
                }
            }
            if (!newUserVouchers.isEmpty()) {
                userVoucherRepository.saveAll(newUserVouchers);
                System.out.println(
                        ">>> Đã seed thành công "
                                + newUserVouchers.size()
                                + " UserVoucher cho Voucher ID = "
                                + targetVoucherId);
            }
        }

        // 4. Sinh Token chuẩn theo đúng thứ tự list user
        List<String> tokens = new ArrayList<>();
        for (User user : k6Users) {
            String token = authenticationService.generateToken(user, false);
            tokens.add(token);
        }

        // 5. Ghi file JSON
        File outputFile = new File("D:\\promo-graud\\performance-test\\scripts\\tokens.json");
        if (outputFile.getParentFile() != null && !outputFile.getParentFile().exists()) {
            outputFile.getParentFile().mkdirs();
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, tokens);

        System.out.println("==================================================");
        System.out.println(" DA TAO THANH CONG " + tokens.size() + " TOKENS CHO K6 USER!");
        System.out.println(" File path: " + outputFile.getAbsolutePath());
        System.out.println("==================================================");
    }
}
