package group2d.promo_graud.modules.products.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PageResponse<T> {
    List<T> data;
    int page;
    int totalPage;
    long total;
}
