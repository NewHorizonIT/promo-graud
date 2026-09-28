package group2d.promo_graud.shared.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class PaginatedResponse<T> {
    private List<T> data;
    private int page;
    private int size;
    private long totalItems;
    private int totalPages;
}
