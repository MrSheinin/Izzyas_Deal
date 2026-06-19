package by.Rsh.dto.dataIngestion;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Builder
@Value
public class SteamGameIdsDto {
    List<Long> appIds;
}
