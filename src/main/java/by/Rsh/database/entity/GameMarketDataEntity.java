package by.Rsh.database.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
