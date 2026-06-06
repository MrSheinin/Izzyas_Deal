package by.Rsh.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.stream.Collectors;

@Component

public class SteamApiClient {
    private static final Logger logger = LoggerFactory.getLogger(SteamApiClient.class);

    private final RestClient client;
    private String apiKey;

    public SteamApiClient(@Value("${steam.api.key}") String apiKey) {
        this.apiKey = apiKey;
        this.client = RestClient.builder().build();
    }

    //=====PARAMETERLESS REQUESTS=====

    public String getBestsellersIds() {
        try {
            return client
                    .get()
                    .uri("https://store.steampowered.com/api/getappsincategory/?category=cat_topsellers&cc=us&l=en")
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            logger.error("Failed to fetch Steam bestsellers: {}", e.getMessage(), e);
        }
        return null;
    }
    public String getTop100popularIds() {
        try {
            return client
                    .get()
                    .uri("https://api.steampowered.com/ISteamChartsService/GetGamesByConcurrentPlayers/v1/?key=" + apiKey)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            logger.error("Failed to fetch Top 100 popular games: {}", e.getMessage(), e);
        }
        return null;
    }


    //=====PARAMETERIZED REQUESTS=====

    public String getPosterLink(Long appId) {
        return String.format("https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/%d/library_600x900.jpg", appId);
    }

    public String getMarketData(List<Long> appIds){
        try {
            String idsParam = appIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));

            return client
                    .get()
                    .uri("https://store.steampowered.com/api/appdetails?appids={ids}&cc=eu&l=en&filters=price_overview", idsParam)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            logger.error("Failed to fetch market data for ids [{}]: {}", appIds, e.getMessage(), e);
        }
        return null;
    }

    public String getDetailedData(Long appId){
        try {
            return client
                    .get()
                    .uri("https://store.steampowered.com/api/appdetails?appids={appId}&cc=eu&l=en", appId)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
        logger.error("Failed to fetch detailed data for id [{}]: {}", appId, e.getMessage(), e);
    }
        return null;
    }
}