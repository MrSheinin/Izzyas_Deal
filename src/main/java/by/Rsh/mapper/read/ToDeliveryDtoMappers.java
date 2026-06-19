package by.Rsh.mapper.read;

import by.Rsh.database.entity.GameEntity;
import by.Rsh.database.entity.GameMarketDataEntity;
import by.Rsh.database.entity.GenreEntity;
import by.Rsh.dto.dataDelivery.GameCardDto;
import by.Rsh.dto.dataDelivery.GameDetailsDto;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class ToDeliveryDtoMappers {
    public GameCardDto toGameCard(GameEntity gameEntity, GameMarketDataEntity gameMarketDataEntity) {
        if (gameEntity != null) {
            return GameCardDto.builder()

                    .appId(gameEntity.getAppId())
                    .name(gameEntity.getName())
                    .type(gameEntity.getType())
                    .headerImageUrl(gameEntity.getHeaderImageUrl())

                    .initialPrice(gameMarketDataEntity != null ? gameMarketDataEntity.getInitialPrice() : null)
                    .finalPrice(gameMarketDataEntity != null ? gameMarketDataEntity.getFinalPrice() : null)
                    .discountPercent(gameMarketDataEntity != null ? gameMarketDataEntity.getDiscountPercent() : null)

                    .build();
        }
        return null;
    }

    public GameDetailsDto toGameDetailsDto(GameEntity gameEntity, GameMarketDataEntity gameMarketDataEntity) {
        if (gameEntity != null) {
            return GameDetailsDto.builder()

                    .appId(gameEntity.getAppId())
                    .name(gameEntity.getName())
                    .type(gameEntity.getType())
                    .description(gameEntity.getDescription())
                    .recommendations(gameEntity.getRecommendations())
                    .windows(gameEntity.getWindows())
                    .mac(gameEntity.getMac())
                    .linux(gameEntity.getLinux())
                    .isComingSoon(gameEntity.getIsComingSoon())
                    .releaseDateParsed(gameEntity.getReleaseDateParsed())
                    .genres(genresToStringList(gameEntity.getGenres()))
                    .headerImageUrl(gameEntity.getHeaderImageUrl())

                    .initialPrice(gameMarketDataEntity != null ? gameMarketDataEntity.getInitialPrice() : null)
                    .finalPrice(gameMarketDataEntity != null ? gameMarketDataEntity.getFinalPrice() : null)
                    .discountPercent(gameMarketDataEntity != null ? gameMarketDataEntity.getDiscountPercent() : null)

                    .build();
        }
        return null;
    }

    private Set<String> genresToStringList(Set<GenreEntity> genreEntities) {
        Set<String> result = new HashSet<>();

        if (genreEntities != null && !genreEntities.isEmpty()) {
            genreEntities.forEach(
                    genreEntity -> result.add(genreEntity.getName())
            );
        }
        return result;
    }
}
