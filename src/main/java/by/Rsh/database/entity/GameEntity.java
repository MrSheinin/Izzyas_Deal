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
import java.util.HashSet;
import java.util.Set;

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
    @Column(columnDefinition = "TEXT")
    private String description;
    private int recommendations;
    private Boolean windows;
    private Boolean mac;
    private Boolean linux;
    private Boolean isComingSoon;
    private LocalDate releaseDateParsed;
    @Column(columnDefinition = "TEXT")
    private String headerImageUrl;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "games_genres",
            joinColumns = @JoinColumn(name = "app_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    @Builder.Default
    private Set<GenreEntity> genres = new HashSet<>();

    @Column(updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;
}
