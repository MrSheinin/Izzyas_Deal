package by.Rsh.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Builder
@Value
public class SteamGameIdsDto {
    List<Long> appIds;
}
