package by.Rsh.database.repository;

import by.Rsh.database.entity.GameMarketDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GameMarketDataRepository extends JpaRepository<GameMarketDataEntity, Long> {

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO game_market_data (app_id, initial_price, final_price, discount_percent, updated_at)
            VALUES (:appId, :initialPrice, :finalPrice, :discountPercent, :updatedAt)
            ON CONFLICT (app_id)
            DO UPDATE SET
                initial_price = EXCLUDED.initial_price,
                final_price = EXCLUDED.final_price,
                discount_percent = EXCLUDED.discount_percent,
                updated_at = EXCLUDED.updated_at
            """, nativeQuery = true)
    void upsertMarketData(
            @Param("appId") Long appId,
            @Param("initialPrice") Integer initialPrice,
            @Param("finalPrice") Integer finalPrice,
            @Param("discountPercent") Integer discountPercent,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    // Лучше сделать красивее по имени:
    List<GameMarketDataEntity> findByInitialPriceGreaterThanAndFinalPriceEquals(Integer minInitial, Integer finalPrice);

    // 2. Игры в определенном ценовом диапазоне (например, "Игры до 500 рублей / 5 евро")
    List<GameMarketDataEntity> findByFinalPriceLessThanEqualOrderByDiscountPercentDesc(Integer maxPrice);

    // 3. Контроль устаревания данных (Важно для шедулера!)
    // Нам нужно будет находить записи, которые НЕ обновлялись больше суток, чтобы выкинуть их или перепроверить
    List<GameMarketDataEntity> findByUpdatedAtBefore(LocalDateTime dateTime);
}
