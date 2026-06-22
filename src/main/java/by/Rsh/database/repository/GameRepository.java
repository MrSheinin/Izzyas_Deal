package by.Rsh.database.repository;

import by.Rsh.database.entity.GameEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface GameRepository extends JpaRepository<GameEntity, Long>, JpaSpecificationExecutor<GameEntity> {

    @Query("SELECT g.appId FROM GameEntity g")
    Set<Long> findAllIds();

    @Query("""
                SELECT DISTINCT g FROM GameEntity g
                JOIN GameMarketDataEntity m ON g.appId = m.appId
                WHERE m.discountPercent >= :minDiscount
                ORDER BY m.discountPercent DESC, g.recommendations DESC NULLS LAST
            """)
    List<GameEntity> findGamesByMinDiscount(@Param("minDiscount") Integer minDiscount);

    @Query("""
                SELECT DISTINCT g FROM GameEntity g
                JOIN GameMarketDataEntity m ON g.appId = m.appId
                WHERE m.finalPrice <= :maxPrice AND m.finalPrice > 0
                ORDER BY g.recommendations DESC NULLS LAST
            """)
    List<GameEntity> findGamesByMaxPrice(@Param("maxPrice") Integer maxPrice);

    @Query("""
                SELECT DISTINCT g FROM GameEntity g
                JOIN GameMarketDataEntity m ON g.appId = m.appId
                WHERE m.discountPercent > 0
                ORDER BY m.discountPercent DESC, g.recommendations DESC NULLS LAST
            """)
    List<GameEntity> findGamesWithAnyDiscount();

    List<GameEntity> findTop50ByIsComingSoonFalseOrderByReleaseDateParsedDesc();

    @Query(value = """
            SELECT g.* FROM games g
            WHERE similarity(g.name, :searchQuery) > 0.2
            ORDER BY 
                CASE 
                    WHEN LOWER(g.name) LIKE LOWER(CONCAT(:searchQuery, '%')) THEN 2.0
                    WHEN LOWER(g.name) LIKE LOWER(CONCAT('%', :searchQuery, '%')) THEN 1.0
                    ELSE similarity(g.name, :searchQuery)
                END DESC,
                g.recommendations DESC NULLS LAST
            LIMIT 6
            """, nativeQuery = true)
    List<GameEntity> findBySimilarName(@Param("searchQuery") String searchQuery);

    List<GameEntity> findByGenresGenreId(Long genreId);
}

