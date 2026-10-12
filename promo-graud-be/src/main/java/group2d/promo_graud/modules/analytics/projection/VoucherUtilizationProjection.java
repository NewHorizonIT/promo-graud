package group2d.promo_graud.modules.analytics.projection;

public interface VoucherUtilizationProjection {
    Integer getVoucherId();

    String getCode();

    String getType();

    Integer getQuantity();

    Integer getQuantityRemain();

    String getStatus();
}
