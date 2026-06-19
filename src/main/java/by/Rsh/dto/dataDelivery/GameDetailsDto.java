package by.Rsh.dto.dataDelivery;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.util.Set;

@Builder
@Value
public class GameDetailsDto {
    Long appId;
    String name;
    String type;
    String description;
    Integer recommendations;
    Boolean windows;
    Boolean mac;
    Boolean linux;
    Boolean isComingSoon;
    LocalDate releaseDateParsed;
    Set<String> genres;
    String headerImageUrl;

    Integer initialPrice;
    Integer finalPrice;
    Integer discountPercent;
}
