package by.Rsh.database.repository;

import by.Rsh.database.entity.GameEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface GameRepository extends JpaRepository<GameEntity, Long> {

    @Query("SELECT g.appId FROM GameEntity g")
    Set<Long> findAllIds();

    List<GameEntity> findTop50ByIsComingSoonFalseOrderByRecommendationsDesc();

    List<GameEntity> findByMacTrue();
    List<GameEntity> findByLinuxTrue();

    List<GameEntity> findByType(String type);

    List<GameEntity> findByGenresGenreId(Long genreId);
}

