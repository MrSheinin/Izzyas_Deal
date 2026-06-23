package by.Rsh.service;

import by.Rsh.database.entity.GameEntity;
import by.Rsh.database.entity.GameMarketDataEntity;
import by.Rsh.database.repository.GameMarketDataRepository;
import by.Rsh.database.repository.GameRepository;
import by.Rsh.database.specification.GameSpecs;
import by.Rsh.dto.dataDelivery.GameCardDto;
import by.Rsh.dto.dataDelivery.GameDetailsDto;
import by.Rsh.dto.dataFromUser.GameSearchFilterDto;
import by.Rsh.mapper.read.ToDeliveryDtoMappers;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameCatalogueService {

    private final GameRepository gameRepository;
    private final GameMarketDataRepository gameMarketDataRepository;
    private final ToDeliveryDtoMappers mappers;


    //==========================
    // -BASIC METHODS (HEADER)-
    //==========================
    public Page<GameCardDto> getActualGames(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return mapGamesToCardDto(gameRepository.findByIsComingSoonFalseOrderByReleaseDateParsedDesc(pageable), pageable);
    }

    public Page<GameCardDto> getAllGamesWithAnyDiscount(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return mapGamesToCardDto(gameRepository.findGamesWithAnyDiscount(pageable), pageable);
    }

    public Page<GameCardDto> getGamesByMinDiscount(Integer minDiscount, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return mapGamesToCardDto(gameRepository.findGamesByMinDiscount(minDiscount, pageable), pageable);
    }

    public Page<GameCardDto> getGamesByMaxPrice(Integer maxPrice, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return mapGamesToCardDto(gameRepository.findGamesByMaxPrice(maxPrice * 100, pageable), pageable);
    }


    //======================
    // -SPECIFIED FILTER-
    //======================
    public Page<GameCardDto> getGamesBySpecifiedFilter(GameSearchFilterDto filterDto, int page, int size) {
        if (filterDto == null) return getActualGames(page, size);

        Specification<GameEntity> spec = Specification.where(GameSpecs.isNotComingSoon());

        spec = spec
                .and(GameSpecs.hasWindows(filterDto.getWindows()))
                .and(GameSpecs.hasMac(filterDto.getMac()))
                .and(GameSpecs.hasLinux(filterDto.getLinux()))
                .and(GameSpecs.hasMinPrice(filterDto.getMinPrice()))
                .and(GameSpecs.hasMaxPrice(filterDto.getMaxPrice()))
                .and(GameSpecs.releasedAfterYear(filterDto.getStartReleaseYear()))
                .and(GameSpecs.hasGenres(filterDto.getGenreIds()));

        Sort sort = Sort.by(Sort.Order.desc("recommendations").nullsLast());

        Pageable pageable = PageRequest.of(page, size, sort);

        return mapGamesToCardDto(gameRepository.findAll(spec, pageable), pageable);
    }

    //======================
    // -SMART SEARCH BAR-
    //======================
    public List<GameCardDto> getGamesBySimilarName(String searchQuery) {
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            return List.of();
        }
        List<GameEntity> foundGames = gameRepository.findBySimilarName(searchQuery.trim());
        return mapGamesToCardDto(foundGames);
    }

    //======================
    // -GAME DETAILED INFO-
    //======================
    public GameDetailsDto getGameDetailed(Long appId) {
        if (appId == null) throw new IllegalArgumentException("App ID cannot be null");

        GameEntity game = gameRepository.findById(appId)
                .orElseThrow(() -> new NoSuchElementException("Game not found with id: " + appId));

        GameMarketDataEntity marketData = gameMarketDataRepository.findById(appId)
                .orElseThrow(() -> new NoSuchElementException("Prices not found for game with id: " + appId));

        return mappers.toGameDetailsDto(game, marketData);
    }

    //======================
    // -MAPPERS-
    //======================
    private List<GameCardDto> mapGamesToCardDto(List<GameEntity> games) {
        if (games == null || games.isEmpty()) {
            return List.of();
        }

        List<Long> appIds = games.stream().map(GameEntity::getAppId).toList();

        Map<Long, GameMarketDataEntity> priceMap = gameMarketDataRepository.findAllById(appIds).stream()
                .collect(Collectors.toMap(GameMarketDataEntity::getAppId, Function.identity()));

        return games.stream()
                .map(game -> mappers.toGameCard(game, priceMap.get(game.getAppId())))
                .toList();
    }

    private Page<GameCardDto> mapGamesToCardDto(Page<GameEntity> gamesPage, Pageable pageable) {
        List<GameCardDto> dtos = mapGamesToCardDto(gamesPage.getContent());
        return new PageImpl<>(dtos, pageable, gamesPage.getTotalElements());
    }
}
