package by.Rsh.controller;

import by.Rsh.dto.dataDelivery.GameCardDto;
import by.Rsh.dto.dataDelivery.GameDetailsDto;
import by.Rsh.dto.dataFromUser.GameSearchFilterDto;
import by.Rsh.service.GameCatalogueService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/games")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class GameCatalogueController {

    private final GameCatalogueService gameCatalogueService;

    @GetMapping
    public ResponseEntity<Page<GameCardDto>> getActualGames(
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(gameCatalogueService.getActualGames(page));
    }

    @GetMapping("/discounts")
    public ResponseEntity<Page<GameCardDto>> getGamesByMinDiscount(
            @RequestParam(required = false) Integer minDiscount,
            @RequestParam(defaultValue = "0") int page) {
        if (minDiscount != null) {
            return ResponseEntity.ok(gameCatalogueService.getGamesByMinDiscount(minDiscount, page));
        }
        return ResponseEntity.ok(gameCatalogueService.getAllGamesWithAnyDiscount(page));
    }

    @GetMapping("/max-price")
    public ResponseEntity<Page<GameCardDto>> getGamesByMaxPrice(
            @RequestParam Integer maxPrice,
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(gameCatalogueService.getGamesByMaxPrice(maxPrice, page));
    }


    @GetMapping("/filter")
    public ResponseEntity<Page<GameCardDto>> getGamesByFilter(
            GameSearchFilterDto filterDto,
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(gameCatalogueService.getGamesBySpecifiedFilter(filterDto, page));
    }


    @GetMapping("/search/live")
    public ResponseEntity<List<GameCardDto>> getGamesBySimilarName(
            @RequestParam String query) {
        return ResponseEntity.ok(gameCatalogueService.getGamesBySimilarName(query));
        //todo реализовать динамическую отправку на сервер и предложение автокоррекции на клиента
    }


    @GetMapping("/{appId}")
    public ResponseEntity<GameDetailsDto> getGameDetailedData(@PathVariable Long appId){
        return ResponseEntity.ok(gameCatalogueService.getGameDetailed(appId));
    }
}
