package by.Rsh.database.repository;

import by.Rsh.database.entity.GameEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface GameRepository extends JpaRepository<GameEntity, Long> {

    @Query("SELECT g.appId FROM GameEntity g")
    Set<Long> findAllIds();

    @Query("""
                SELECT DISTINCT g FROM GameEntity g
                JOIN GameMarketDataEntity m ON g.appId = m.appId
                WHERE m.discountPercent >= :minDiscount
                ORDER BY m.discountPercent DESC
            """)
    List<GameEntity> findGamesByMinDiscount(@Param("minDiscount") Integer minDiscount);

    @Query("""
                SELECT DISTINCT g FROM GameEntity g
                JOIN GameMarketDataEntity m ON g.appId = m.appId
                WHERE m.finalPrice <= :maxPrice AND m.finalPrice > 0
            """)
    List<GameEntity> findGamesByMaxPrice(@Param("maxPrice") Integer maxPrice);

    @Query("""
                SELECT DISTINCT g FROM GameEntity g
                JOIN GameMarketDataEntity m ON g.appId = m.appId
                WHERE m.discountPercent > 0
                ORDER BY m.discountPercent DESC
            """)
    List<GameEntity> findGamesWithAnyDiscount();

    List<GameEntity> findTop50ByIsComingSoonFalseOrderByReleaseDateParsedDesc();

    List<GameEntity> findByGenresGenreId(Long genreId);
}

