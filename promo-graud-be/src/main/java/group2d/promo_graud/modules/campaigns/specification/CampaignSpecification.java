package group2d.promo_graud.modules.campaigns.specification;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import group2d.promo_graud.modules.campaigns.Campaign;
import group2d.promo_graud.modules.campaigns.dto.CampaignSearchRequest;

public class CampaignSpecification {

    public static Specification<Campaign> filter(CampaignSearchRequest request) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Luôn lọc các bản ghi chưa bị xóa
            predicates.add(cb.equal(root.get("isDeleted"), false));

            // 2. Lọc theo Name (dùng StringUtils để check rỗng gọn hơn)
            if (StringUtils.hasText(request.getName())) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("name")),
                                "%" + request.getName().trim().toLowerCase() + "%"));
            }

            // 3. Lọc theo Status
            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }

            // 4. Lọc theo StartTime (startTime >= request.startTime)
            if (request.getStartTime() != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(root.get("startTime"), request.getStartTime()));
            }

            // 5. Lọc theo EndTime (endTime <= request.endTime)
            if (request.getEndTime() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("endTime"), request.getEndTime()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
