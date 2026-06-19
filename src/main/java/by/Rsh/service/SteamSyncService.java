package by.Rsh.service;

import by.Rsh.client.SteamApiClient;
import by.Rsh.database.entity.GameEntity;
import by.Rsh.database.entity.GenreEntity;
import by.Rsh.database.repository.GameMarketDataRepository;
import by.Rsh.database.repository.GameRepository;
import by.Rsh.database.repository.GenreRepository;
import by.Rsh.dto.dataIngestion.SteamGameDetailsDto;
import by.Rsh.dto.dataIngestion.SteamGenreDto;
import by.Rsh.dto.dataIngestion.SteamPriceBatchDto;
import by.Rsh.dto.dataIngestion.SteamPriceDto;
import by.Rsh.mapper.SteamJsonMappers;
import by.Rsh.mapper.create.ToEntityMappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SteamSyncService {

    private final SteamApiClient steamClient;
    private final SteamJsonMappers jsonMappers;
    private final ToEntityMappers entityMappers;

    private final GameRepository gameRepository;
    private final GameMarketDataRepository gameMarketDataRepository;
    private final GenreRepository genreRepository;

    /**
     * Main synchronization pipeline triggered on application startup.
     * Orchestrates fetching IDs, validating prices, and updating game metadata.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void syncPipeline() {
        log.info("Starting Steam synchronization pipeline...");

        // 1. Fetch IDs from Steam charts (Bestsellers and Popular)
        List<Long> rawAppIds = loadGameIds();

        // Best practice: Handle empty results early to avoid unnecessary processing
        if (rawAppIds == null || rawAppIds.isEmpty()) {
            log.warn("No app IDs retrieved from Steam API. Aborting sync.");
            rawAppIds = List.of();
        }

        Set<Long> existingAppIds = gameRepository.findAllIds();
        Set<Long> allIdsToUpdatePrice = new HashSet<>(rawAppIds);
        allIdsToUpdatePrice.addAll(existingAppIds);

        log.info("Sync scope: [Steam charts: {}] | [DB existing: {}] | [Total unique: {}]",
                rawAppIds.size(), existingAppIds.size(), allIdsToUpdatePrice.size());

        if (allIdsToUpdatePrice.isEmpty()) {
            log.info("No app IDs found to process. Synchronization finished.");
            return;
        }

        // 2. Fetch current market prices for all identified IDs
        Map<Long, SteamPriceDto> allPricesMap = loadPrices(new ArrayList<>(allIdsToUpdatePrice));

        List<Long> filteredAppIds = filterValidIds(allPricesMap);
        log.info("Filtration complete: {}/{} IDs are valid for processing", filteredAppIds.size(), allIdsToUpdatePrice.size());

        // 3. Process static data (New games only)
        for (Long appId : filteredAppIds) {
            try {
                if (!existingAppIds.contains(appId)) {
                    SteamGameDetailsDto gameDetailsDto = loadGameDetails(appId);
                    if (gameDetailsDto != null) {
                        GameEntity gameEntity = entityMappers.toEntity(appId, gameDetailsDto);
                        gameEntity.setHeaderImageUrl(steamClient.getPosterLink(appId));

                        Set<GenreEntity> managedGenres = getOrCreateGenres(gameDetailsDto.getGenres());
                        gameEntity.setGenres(managedGenres);

                        gameRepository.save(gameEntity);
                        log.debug("Successfully imported new game: {} (ID: {})", gameEntity.getName(), appId);

                        // Respect Steam API rate limits
                        sleep(2000);
                    }
                }
            } catch (Exception e) {
                // Best practice: Log stack trace only if necessary, otherwise keep it concise
                log.error("Failed to process static data for appId {}: {}", appId, e.getMessage(), e);
            }
        }

        // 4. Update dynamic market data (Prices/Discounts)
        log.info("Updating market prices for {} valid games...", filteredAppIds.size());
        for (Long validAppId : filteredAppIds) {
            try {
                SteamPriceDto priceDto = allPricesMap.get(validAppId);
                if (priceDto != null) {
                    gameMarketDataRepository.upsertMarketData(
                            validAppId,
                            priceDto.getInitialPrice(),
                            priceDto.getFinalPrice(),
                            priceDto.getDiscountPercent(),
                            LocalDateTime.now()
                    );
                }
            } catch (Exception e) {
                log.error("Failed to upsert market data for appId {}: {}", validAppId, e.getMessage(), e);
            }
        }
        log.info("Steam synchronization pipeline completed successfully.");
    }

    //==========HELPERS==========

    /**
     * Aggregates App IDs from multiple Steam Storefront endpoints.
     */
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

    /**
     * Fetches prices in batches to optimize network calls and respect API constraints.
     */
    private Map<Long, SteamPriceDto> loadPrices(List<Long> ids) {
        Map<Long, SteamPriceDto> allPricesMap = new HashMap<>();
        final int batchSize = 20;
        for (int i = 0; i < ids.size(); i += batchSize) {
            int end = Math.min(i + batchSize, ids.size());
            List<Long> batch = ids.subList(i, end);
            try {
                String marketBatchJson = steamClient.getMarketData(batch);
                if (marketBatchJson != null) {
                    SteamPriceBatchDto batchDto = jsonMappers.toSteamPriceBatchDto(marketBatchJson);
                    if (batchDto != null && batchDto.getSteamPriceBatch() != null) {
                        allPricesMap.putAll(batchDto.getSteamPriceBatch());
                    }
                }
                sleep(250);

            } catch (Exception e) {
                log.error("Error loading price batch at index {}: {}", i, e.getMessage(), e);
            }
        }
        return allPricesMap;
    }

    /**
     * Filters out IDs that Steam reported as unsuccessful or missing data.
     */
    private List<Long> filterValidIds(Map<Long, SteamPriceDto> priceBatch) {
        if (priceBatch == null || priceBatch.isEmpty()) {
            return List.of();
        }
        return priceBatch.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .filter(entry -> entry.getValue().isSuccess())
                .map(Map.Entry::getKey)
                .toList();
    }

    /**
     * Fetches detailed metadata for a specific App ID.
     */
    private SteamGameDetailsDto loadGameDetails(Long appId) {
        if (appId == null) {
            return null;
        }
        try {
            String gameDetailsJson = steamClient.getDetailedData(appId);
            if (gameDetailsJson != null) {
                return jsonMappers.toSteamGameDetailsDto(gameDetailsJson);
            }
        } catch (Exception e) {
            log.error("Error loading game details for appId {}: {}", appId, e.getMessage(), e);
        }
        return null;
    }

    /**
     * Ensures genres exist in the database, creating them if they are new.
     */
    @Transactional
    protected Set<GenreEntity> getOrCreateGenres(List<SteamGenreDto> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return Set.of();
        }

        Set<Long> incomingGenreIds = dtoList.stream()
                .map(SteamGenreDto::getGenreId)
                .collect(Collectors.toSet());

        List<GenreEntity> existingGenres = genreRepository.findAllById(incomingGenreIds);

        Map<Long, GenreEntity> existingGenresMap = existingGenres.stream()
                .collect(Collectors.toMap(GenreEntity :: getGenreId, genre -> genre));

        Set<GenreEntity> managedGenres = new HashSet<>();

        for (SteamGenreDto genreDto : dtoList) {
            Long id = genreDto.getGenreId();

            if (existingGenresMap.containsKey(id)) {
                managedGenres.add(existingGenresMap.get(id));
            } else {
                log.info("Registering new genre: [{}] {}", id, genreDto.getName());
                GenreEntity newGenre = genreRepository.save(
                        GenreEntity.builder()
                                .genreId(id)
                                .name(genreDto.getName())
                                .build()
                );
                managedGenres.add(newGenre);
                existingGenresMap.put(id,newGenre);
            }
        }
        return managedGenres;
    }

    private void sleep(long milliseconds){
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            log.warn("Synchronization thread was interrupted during sleep", e);
            Thread.currentThread().interrupt();
        }
    }
}
