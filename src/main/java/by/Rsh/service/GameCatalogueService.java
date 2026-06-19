package by.Rsh.service;

import by.Rsh.database.entity.GameEntity;
import by.Rsh.database.entity.GameMarketDataEntity;
import by.Rsh.database.repository.GameMarketDataRepository;
import by.Rsh.database.repository.GameRepository;
import by.Rsh.dto.dataDelivery.GameCardDto;
import by.Rsh.dto.dataDelivery.GameDetailsDto;
import by.Rsh.mapper.read.ToDeliveryDtoMappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameCatalogueService {

    private final GameRepository gameRepository;
    private final GameMarketDataRepository gameMarketDataRepository;
    private final ToDeliveryDtoMappers mappers;


    //======================
    //BASIC METHODS (HEADER)
    //======================
    public List<GameCardDto> getActualGames() {
        List<GameEntity> games = gameRepository.findTop50ByIsComingSoonFalseOrderByReleaseDateParsedDesc();
        return mapGamesToCardDto(games);
    }

    public List<GameCardDto> getAllGamesWithAnyDiscount(){
        List<GameEntity> games = gameRepository.findGamesWithAnyDiscount();
        return mapGamesToCardDto(games);
    }

    public List<GameCardDto> getGamesByMinDiscount(Integer minDiscount){
        List<GameEntity> games = gameRepository.findGamesByMinDiscount(minDiscount);
        return mapGamesToCardDto(games);
    }

    public List<GameCardDto> getGamesByMaxPrice(Integer maxPrice){
        List<GameEntity> games = gameRepository.findGamesByMaxPrice(maxPrice * 100);
        return mapGamesToCardDto(games);
    }







    
    //======================
    //MAPPERS
    //======================

    private List<GameCardDto> mapGamesToCardDto(List<GameEntity> games) {
        if (games == null || games.isEmpty()) {
            return List.of();
        }

        Map<Long, GameMarketDataEntity> priceMap = mapGames(games);
        List<GameCardDto> cardDtos = new ArrayList<>();

        for (GameEntity game : games) {
            GameMarketDataEntity marketData = priceMap.get(game.getAppId());
            cardDtos.add(mappers.toGameCard(game, marketData));
        }

        return cardDtos;
    }

    private List<GameDetailsDto> mapToGamesDetailsDto(List<GameEntity> games) {
        if (games == null || games.isEmpty()) {
            return List.of();
        }

        Map<Long, GameMarketDataEntity> priceMap = mapGames(games);
        List<GameDetailsDto> detailsDtos = new ArrayList<>();

        for (GameEntity game : games) {
            GameMarketDataEntity marketData = priceMap.get(game.getAppId());
            detailsDtos.add(mappers.toGameDetailsDto(game, marketData));
        }

        return detailsDtos;
    }

    private Map<Long, GameMarketDataEntity> mapGames(List<GameEntity> games) {
        if (games == null || games.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> appIds = games.stream()
                .map(GameEntity::getAppId)
                .toList();

        List<GameMarketDataEntity> prices = gameMarketDataRepository.findAllById(appIds);

        return prices.stream()
                .collect(Collectors.toMap(GameMarketDataEntity::getAppId, Function.identity()));
    }
}
