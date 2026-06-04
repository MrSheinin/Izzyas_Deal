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
    boolean windows;
    boolean mac;
    boolean linux;
    boolean isComingSoon;
    LocalDate releaseDateParsed;
    String headerImageUrl;
}
