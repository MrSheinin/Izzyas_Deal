package by.Rsh.database.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "game_market_data")

public class GameMarketDataEntity {
    @Id
    private Long appId;
    private Integer initialPrice;
    private Integer finalPrice;
    private Integer discountPercent;
    private LocalDateTime updatedAt;
}
