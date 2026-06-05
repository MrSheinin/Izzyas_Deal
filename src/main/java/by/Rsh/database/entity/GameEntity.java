package by.Rsh.database.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "games")
public class GameEntity {
    @Id
    Long appId;
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
