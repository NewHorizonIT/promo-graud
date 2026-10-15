package group2d.promo_graud.modules.voucher.dto.responses;

// SELECT các cột cần thiết thay vì load toàn bộ Entity.
// Tận dụng Spring Data JPA Dynamic Proxy để tự động map dữ liệu từ DB,
// tránh tạo Class DTO cồng kềnh hoặc map Object[] thủ công.
public interface VoucherCardResponse {
    Integer getRuleId();

    String getRuleName();

    String getCampaignName();

    String getChannel();

    Long getTotalUniqueVouchers(); // dùng Long vì Count trả về kiểu long
}
