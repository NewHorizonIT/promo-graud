package group2d.promo_graud.modules.user.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import group2d.promo_graud.modules.user.dto.request.UserVoucherRequest;
import group2d.promo_graud.modules.user.dto.response.UserVoucherResponse;
import group2d.promo_graud.modules.user.entity.User;
import group2d.promo_graud.modules.user.entity.UserVoucher;
import group2d.promo_graud.modules.user.error.UserErrorCode;
import group2d.promo_graud.modules.user.error.UserVoucherErrorCode;
import group2d.promo_graud.modules.user.repository.UserRepository;
import group2d.promo_graud.modules.user.repository.UserVoucherRepository;
import group2d.promo_graud.modules.voucher.Voucher;
import group2d.promo_graud.modules.voucher.VoucherRepository;
import group2d.promo_graud.modules.voucher.dto.responses.VoucherResponse;
import group2d.promo_graud.modules.voucher.enums.VoucherErrorCode;
import group2d.promo_graud.modules.voucher.enums.VoucherStatus;
import group2d.promo_graud.modules.voucher.enums.VoucherType;
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

        if (voucher.getType() == VoucherType.UNIQUE) {
            throw new AppException(UserVoucherErrorCode.USER_VOUCHER_ALREADY_CLAIMED);
        }

        if (voucher.getStatus() != VoucherStatus.ACTIVE) {
            throw new AppException(UserVoucherErrorCode.USER_VOUCHER_INACTIVE);
        }

        if (voucher.getQuantityRemain() <= 0) {
            throw new AppException(UserVoucherErrorCode.USER_VOUCHER_VOUCHER_OUT_STOCK);
        }

        if (userVoucherRepository.existsByUserIdAndVoucherId(user.getId(), voucher.getId())) {
            throw new AppException(UserVoucherErrorCode.USER_VOUCHER_ALREADY_CLAIMED);
        }

        UserVoucher userVoucher = UserVoucher.builder().user(user).voucher(voucher).build();
        return mapToUserVoucherResponse(userVoucherRepository.save(userVoucher));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Page<UserVoucherResponse> findAll(int page, int limit) {
        Pageable pageAble = PageRequest.of(page, limit);
        Page<UserVoucher> list = userVoucherRepository.findAll(pageAble);
        return list.map(this::mapToUserVoucherResponse);
    }

    public User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user =
                userRepository
                        .findByUsername(authentication.getName())
                        .orElseThrow(() -> new AppException(UserErrorCode.USER_NOT_EXISTS));
        if (user.getIsDeleted()) {
            throw new AppException(UserErrorCode.USER_NOT_EXISTS);
        }
        return user;
    }

    public Voucher getVoucher(Integer voucherId) {
        return voucherRepository
                .findById(voucherId)
                .orElseThrow(() -> new AppException(VoucherErrorCode.NOT_EXIST));
    }

    public UserVoucherResponse mapToUserVoucherResponse(UserVoucher userVoucher) {
        return UserVoucherResponse.builder()
                .id(userVoucher.getId())
                .userId(userVoucher.getUser().getId())
                .voucher(VoucherResponse.fromEntity(userVoucher.getVoucher()))
                .collectedAt(userVoucher.getCollectedAt())
                .status(userVoucher.getStatus())
                .build();
    }

    public Page<UserVoucherResponse> getMyVoucher(Integer userId, Integer page, Integer limit) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, limit));
        Page<UserVoucher> userVouchers = userVoucherRepository.findAllByUserId(userId, pageable);

        return userVouchers.map(this::mapToUserVoucherResponse);
    }
}
