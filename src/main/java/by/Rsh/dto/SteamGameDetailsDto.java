package by.Rsh.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

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
    String headerImageUrl;
}
