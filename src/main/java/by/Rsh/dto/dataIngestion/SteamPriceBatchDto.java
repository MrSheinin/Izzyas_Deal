package by.Rsh.dto.dataIngestion;

import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Builder
@Value
public class SteamPriceBatchDto {
    Map<Long, SteamPriceDto> steamPriceBatch;
}
