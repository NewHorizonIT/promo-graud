package group2d.promo_graud.modules.campaigns.specification;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import group2d.promo_graud.modules.campaigns.Campaign;
import group2d.promo_graud.modules.campaigns.dto.CampaignSearchRequest;

public class CampaignSpecification {

    public static Specification<Campaign> filter(CampaignSearchRequest request) {

        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.equal(root.get("isDeleted"), false);

            if (request.getName() != null && !request.getName().trim().isEmpty()) {

                predicate =
                        criteriaBuilder.and(
                                predicate,
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(root.get("name")),
                                        "%" + request.getName().trim().toLowerCase() + "%"));
            }

            if (request.getStatus() != null) {

                predicate =
                        criteriaBuilder.and(
                                predicate,
                                criteriaBuilder.equal(root.get("status"), request.getStatus()));
            }

            if (request.getStartTime() != null) {

                predicate =
                        criteriaBuilder.and(
                                predicate,
                                criteriaBuilder.lessThanOrEqualTo(
                                        root.get("startTime"), request.getEndTime()));
            }

            if (request.getEndTime() != null) {

                predicate =
                        criteriaBuilder.and(
                                predicate,
                                criteriaBuilder.greaterThanOrEqualTo(
                                        root.get("endTime"), request.getStartTime()));
            }

            return predicate;
        };
    }
}
