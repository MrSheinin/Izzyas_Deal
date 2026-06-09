package by.Rsh.database.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "games")
@EntityListeners(AuditingEntityListener.class)
public class GameEntity {
    @Id
    private Long appId;
    private String name;
    private String type;
    private String description;
    private Integer recommendations;
    private Boolean windows;
    private Boolean mac;
    private Boolean linux;
    private Boolean isComingSoon;
    private LocalDate releaseDateParsed;
    private String headerImageUrl;
    @Column(updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;
}
