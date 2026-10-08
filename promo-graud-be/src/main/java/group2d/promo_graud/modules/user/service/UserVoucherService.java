package group2d.promo_graud.modules.user.service;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import group2d.promo_graud.modules.user.dto.request.UserVoucherRequest;
import group2d.promo_graud.modules.user.dto.response.UserVoucherResponse;
import group2d.promo_graud.modules.user.entity.User;
import group2d.promo_graud.modules.user.entity.UserVoucher;
import group2d.promo_graud.modules.user.error.UserErrorCode;
import group2d.promo_graud.modules.user.repository.UserRepository;
import group2d.promo_graud.modules.user.repository.UserVoucherRepository;
import group2d.promo_graud.modules.voucher.Voucher;
import group2d.promo_graud.modules.voucher.VoucherRepository;
import group2d.promo_graud.modules.voucher.enums.VoucherErrorCode;
import group2d.promo_graud.shared.exception.AppException;

@Service
@RequiredArgsConstructor
public class UserVoucherService {
    private final UserVoucherRepository userVoucherRepository;
    private final UserRepository userRepository;
    private final VoucherRepository voucherRepository;

    public UserVoucherResponse create(UserVoucherRequest request) {
        User user = getUser();
        Voucher voucher = getVoucher(request.getVoucherId());
        UserVoucher userVoucher =
                UserVoucher.builder()
                        .user(user)
                        .voucher(voucher)
                        .collectedAt(request.getCollectedAt())
                        .status(request.getStatus())
                        .build();
        return mapToUserVoucherResponse(userVoucherRepository.save(userVoucher));
    }

    public List<UserVoucherResponse> findAll() {
        List<UserVoucher> list = userVoucherRepository.findAll();
        return list.stream().map(this::mapToUserVoucherResponse).toList();
    }

    public User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userRepository
                .findByUsername(authentication.getName())
                .orElseThrow(() -> new AppException(UserErrorCode.USER_NOT_EXISTS));
    }

    public Voucher getVoucher(Integer voucherId) {
        return voucherRepository
                .findById(voucherId)
                .orElseThrow(() -> new AppException(VoucherErrorCode.NOT_EXIST));
    }

    public UserVoucherResponse mapToUserVoucherResponse(UserVoucher userVoucher) {
        return UserVoucherResponse.builder()
                .id(userVoucher.getId())
                .user(userVoucher.getUser())
                .voucher(userVoucher.getVoucher())
                .collectedAt(userVoucher.getCollectedAt())
                .status(userVoucher.getStatus())
                .build();
    }
}
