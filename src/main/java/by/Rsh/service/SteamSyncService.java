package by.Rsh.service;

import by.Rsh.client.SteamApiClient;
import by.Rsh.database.entity.GameEntity;
import by.Rsh.database.repository.GameMarketDataRepository;
import by.Rsh.database.repository.GameRepository;
import by.Rsh.dto.SteamGameDetailsDto;
import by.Rsh.dto.SteamPriceBatchDto;
import by.Rsh.dto.SteamPriceDto;
import by.Rsh.mapper.SteamJsonMappers;
import by.Rsh.mapper.create.ToEntityMappers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@lombok.RequiredArgsConstructor
public class SteamSyncService {

    private final SteamApiClient steamClient;
    private final SteamJsonMappers jsonMappers;
    private final ToEntityMappers entityMappers;

    private final GameRepository gameRepository;
    private final GameMarketDataRepository gameMarketDataRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void syncPipeline(){
        log.info("--- START STEAM SYNCHRONIZATION PIPELINE ---");

        List<Long> rawAppIds = loadGameIds();
        if (rawAppIds == null || rawAppIds.isEmpty()){
            log.warn("No app IDs retrieved from Steam API. Aborting sync.");
            rawAppIds = List.of();
        }

        Set<Long> existingAppIds = gameRepository.findAllIds();
        Set<Long> allIdsToUpdatePrice = new HashSet<>(rawAppIds);
        allIdsToUpdatePrice.addAll(existingAppIds);

        log.info("Steam top charts: {} ids | DB existing games: {} ids | Total unique ids to check: {}",
                rawAppIds.size(), existingAppIds.size(), allIdsToUpdatePrice.size());

        if (allIdsToUpdatePrice.isEmpty()) {
            log.warn("No app IDs to process. Aborting sync.");
            return;
        }

        Map<Long, SteamPriceDto> allPricesMap = loadPrices(new ArrayList<>(allIdsToUpdatePrice));

        List<Long> filteredAppIds = filterValidIds(allPricesMap);
        log.info("Total raw Ids: {} | Valid Ids after filtration: {}", rawAppIds.size(), filteredAppIds.size());

        for (Long appId : filteredAppIds){
            try {
                if (!existingAppIds.contains(appId)){
                    SteamGameDetailsDto gameDetailsDto = loadGameDetails(appId);
                    if (gameDetailsDto != null){
                        GameEntity gameEntity = entityMappers.toEntity(appId, gameDetailsDto);
                        gameEntity.setHeaderImageUrl(steamClient.getPosterLink(appId));

                        gameRepository.save(gameEntity);
                        log.info("Saved static data for new game: {}", gameEntity.getName());
                    }
                }
            } catch (Exception e) {
                log.error("Failed to process static data for appId {}: {}", appId, e.getMessage());
            }
        }
        log.info("Updating market prices for valid games");
        for (Long validAppId : filteredAppIds){
            try {
                SteamPriceDto priceDto = allPricesMap.get(validAppId);
                if (priceDto != null){
                    gameMarketDataRepository.upsertMarketData(
                            validAppId,
                            priceDto.getInitialPrice(),
                            priceDto.getFinalPrice(),
                            priceDto.getDiscountPercent(),
                            LocalDateTime.now()
                    );
                }
            } catch (Exception e) {
                log.error("Failed to upsert market data for appId {}: {}", validAppId, e.getMessage());
            }
        }
    }

    //==========HELPERS==========
    private List<Long> loadGameIds() {
        String bestsellersJson;
        try {
            bestsellersJson = steamClient.getBestsellersIds();
        } catch (Exception e) {
            log.error("Failed to fetch bestsellers: {}", e.getMessage());
            bestsellersJson = "";
        }
        String popularGamesJson;
        try {
            popularGamesJson = steamClient.getTop100popularIds();
        } catch (Exception e) {
            log.error("Failed to fetch popular games: {}", e.getMessage());
            popularGamesJson = "";
        }

        return jsonMappers.toSteamGameIdsDto(List.of(bestsellersJson, popularGamesJson)).getAppIds();
    }

    private Map<Long, SteamPriceDto> loadPrices(List<Long> ids){
        Map<Long, SteamPriceDto> allPricesMap = new HashMap<>();
        final int batchSize = 20;
        for (int i = 0; i < ids.size(); i += batchSize){
            int end = Math.min(i + batchSize, ids.size());
            List<Long> batch = ids.subList(i, end);
            try {
                String marketBatchJson = steamClient.getMarketData(batch);
                if (marketBatchJson != null){
                    SteamPriceBatchDto batchDto = jsonMappers.toSteamPriceBatchDto(marketBatchJson);
                    if (batchDto != null && batchDto.getSteamPriceBatch() != null){
                        allPricesMap.putAll(batchDto.getSteamPriceBatch());
                    }
                }
            } catch (Exception e) {
                log.error("Error loading price batch starting at index {}: {}", i, e.getMessage());
            }
        }
        return allPricesMap;
    }

    private List<Long> filterValidIds(Map<Long, SteamPriceDto> priceBatch){
        if (priceBatch == null || priceBatch.isEmpty()){
            return List.of();
        }
        return priceBatch.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .filter(entry -> entry.getValue().isSuccess())
                .map(Map.Entry::getKey)
                .toList();
    }


    private SteamGameDetailsDto loadGameDetails(Long appId){
        if (appId == null){
            return null;
        }
        try {
            String gameDetailsJson = steamClient.getDetailedData(appId);
            if (gameDetailsJson != null){
                return jsonMappers.toSteamGameDetailsDto(gameDetailsJson);
            }
        } catch (Exception e) {
            log.error("Error loading game details for appId {}: {}", appId, e.getMessage());
        }
        return null;
    }
}
