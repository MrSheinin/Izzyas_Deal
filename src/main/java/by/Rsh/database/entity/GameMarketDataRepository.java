package by.Rsh.database.entity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface GameMarketDataRepository extends JpaRepository<GameMarketDataEntity, Long> {
    // 1. Поиск 100% скидок (Раздачи free-to-play или временная халява: finalPrice == 0 при исходной > 0)
    List<GameMarketDataEntity> findByFinalPriceWithDiscountEquals(Integer finalPrice);
    // Лучше сделать красивее по имени:
    List<GameMarketDataEntity> findByInitialPriceGreaterThanAndFinalPriceEquals(Integer minInitial, Integer finalPrice);

    // 2. Игры в определенном ценовом диапазоне (например, "Игры до 500 рублей / 5 евро")
    List<GameMarketDataEntity> findByFinalPriceLessThanEqualOrderByDiscountPercentDesc(Integer maxPrice);

    // 3. Контроль устаревания данных (Важно для шедулера!)
    // Нам нужно будет находить записи, которые НЕ обновлялись больше суток, чтобы выкинуть их или перепроверить
    List<GameMarketDataEntity> findByUpdatedAtBefore(LocalDateTime dateTime);
}
