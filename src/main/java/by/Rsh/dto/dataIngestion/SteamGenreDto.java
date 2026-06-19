package by.Rsh.dto.dataIngestion;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class SteamGenreDto {
    Long genreId;
    String name;
}
