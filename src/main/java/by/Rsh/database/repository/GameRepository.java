package by.Rsh.database.repository;

import by.Rsh.database.entity.GameEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameRepository extends JpaRepository<GameEntity, Long> {

    List<GameEntity> findTop50ByIsComingSoonFalseOrderByRecommendationsDesc();

    List<GameEntity> findByMacTrue();
    List<GameEntity> findByLinuxTrue();

    List<GameEntity> findByType(String type);
}
