package by.Rsh.dto.dataDelivery;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class GameCardDto {
    Long appId;
    String type;
    String name;
    String headerImageUrl;
    Integer initialPrice;
    Integer finalPrice;
    Integer discountPercent;
}
