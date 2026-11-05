package group2d.promo_graud.modules.voucher.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import group2d.promo_graud.modules.voucher.Voucher;
import group2d.promo_graud.modules.voucher.VoucherRepository;

@Service
@RequiredArgsConstructor
public class VoucherInsertHelper {

    private final VoucherRepository voucherRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Voucher insertSingle(Voucher voucher) {
        return voucherRepository.saveAndFlush(voucher);
    }
}
