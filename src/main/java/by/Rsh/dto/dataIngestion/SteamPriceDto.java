package by.Rsh.dto.dataIngestion;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class SteamPriceDto {
    Integer initialPrice;
    Integer finalPrice;
    Integer discountPercent;
    boolean isSuccess;
}
