package by.Rsh.dto.dataIngestion;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.util.List;

@Builder
@Value
public class SteamGameDetailsDto {
    String name;
    String type;
    String description;
    Integer recommendations;
    Boolean windows;
    Boolean mac;
    Boolean linux;
    Boolean isComingSoon;
    LocalDate releaseDateParsed;
    List<SteamGenreDto> genres;
    String headerImageUrl;
}
