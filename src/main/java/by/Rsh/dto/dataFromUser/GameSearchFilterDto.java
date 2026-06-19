package by.Rsh.dto.dataFromUser;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class GameSearchFilterDto {
    private List<Long> genreIds;

    private Boolean windows;
    private Boolean mac;
    private Boolean linux;

    private Integer startReleaseYear;

    private Boolean sortByRecommendations;

    private Integer minPrice;
    private Integer maxPrice;
}
