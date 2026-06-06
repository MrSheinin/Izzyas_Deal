package by.Rsh.mapper.create;

import by.Rsh.database.entity.GameEntity;
import by.Rsh.database.entity.GameMarketDataEntity;
import by.Rsh.dto.SteamGameDetailsDto;
import by.Rsh.dto.SteamPriceDto;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ToEntityMappers {
    public GameEntity toEntity(Long appId, SteamGameDetailsDto dto){
        return GameEntity.builder()
                .appId(appId)
                .name(dto.getName())
                .type(dto.getType())
                .description(dto.getDescription())
                .recommendations(dto.getRecommendations())
                .windows(dto.getWindows())
                .mac(dto.getMac())
                .linux(dto.getLinux())
                .isComingSoon(dto.getIsComingSoon())
                .headerImageUrl(dto.getHeaderImageUrl())
                .releaseDateParsed(dto.getReleaseDateParsed())
                .build();
    }

    public GameMarketDataEntity toEntity(Long appId, SteamPriceDto dto){
        return GameMarketDataEntity.builder()
                .appId(appId)
                .initialPrice(dto.getInitialPrice())
                .finalPrice(dto.getFinalPrice())
                .discountPercent(dto.getDiscountPercent())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
