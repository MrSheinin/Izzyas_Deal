package by.Rsh.mapper;

import by.Rsh.dto.SteamGameDetailsDto;
import by.Rsh.dto.SteamGameIdsDto;
import by.Rsh.dto.SteamPriceBatchDto;
import by.Rsh.dto.SteamPriceDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class SteamJsonMappers {

    private static final Set<String> ID_KEYS = Set.of("id", "appId", "appid");
    private static final Logger logger = LoggerFactory.getLogger(SteamJsonMappers.class);

    private final ObjectMapper objectMapper;

    public SteamJsonMappers(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    //=====MAPPERS=====

    public SteamGameIdsDto toSteamGameIdsDto(List<String> jsonStrings) {
        Set<Long> uniqueIds = new HashSet<>();
        if (jsonStrings == null || jsonStrings.isEmpty()) {
            return SteamGameIdsDto.builder().appIds(List.of()).build();
        }

        for (String json : jsonStrings) {
            if (json == null || json.isBlank()) {
                continue;
            }
            try {
                JsonNode rootNode = objectMapper.readTree(json);
                extractIdsRecursively(rootNode, uniqueIds);
            } catch (JsonProcessingException e) {
                logger.error("Error processing JSON for game IDs: {}", e.getMessage(), e);
            }
        }
        return SteamGameIdsDto.builder().appIds(uniqueIds.stream().toList()).build();
    }

    public SteamPriceBatchDto toSteamPriceBatchDto(String json) {
        if (json == null || json.isBlank()) {
            return SteamPriceBatchDto.builder().steamPriceBatch(Map.of()).build();
        }
        try {
            Map<Long, JsonNode> rootMap = objectMapper.readValue(json, new TypeReference<Map<Long, JsonNode>>() {
            });

            Map<Long, SteamPriceDto> finalPriceBatch = new HashMap<>();

            rootMap.forEach((appId, appNode) -> {
                boolean success = appNode.path("success").asBoolean(false);
                SteamPriceDto.SteamPriceDtoBuilder priceBuilder = SteamPriceDto.builder().isSuccess(success);

                if (success && appNode.has("data") && appNode.path("data").has("price_overview")) {
                    JsonNode priceOverview = appNode.path("data").path("price_overview");
                    priceBuilder
                            .initialPrice(priceOverview.path("initial").asInt())
                            .finalPrice(priceOverview.path("final").asInt())
                            .discountPercent(priceOverview.path("discount_percent").asInt());
                } else {
                    priceBuilder.initialPrice(0).finalPrice(0).discountPercent(0);
                }
                finalPriceBatch.put(appId, priceBuilder.build());
            });
            return SteamPriceBatchDto.builder()
                    .steamPriceBatch(finalPriceBatch)
                    .build();

        } catch (IOException e) {
            logger.error("Critical error parsing Steam price batch JSON: {}", e.getMessage(), e);
            return SteamPriceBatchDto.builder().steamPriceBatch(Map.of()).build();
        }
    }

    public SteamGameDetailsDto toSteamGameDetailsDto(String json){
        if (json == null || json.isBlank()){
            return null;
        }

        try {
            JsonNode rootNode = objectMapper.readTree(json);
            if (!rootNode.fieldNames().hasNext()){
                return null;
            }

            String appId = rootNode.fieldNames().next();
            JsonNode appNode = rootNode.get(appId);

            boolean success = appNode.path("success").asBoolean(false);
            if (!success) return null;

            JsonNode dataNode = appNode.path("data");

            JsonNode platforms = dataNode.path("platforms");
            boolean windows = platforms.path("windows").asBoolean(false);
            boolean mac = platforms.path("mac").asBoolean(false);
            boolean linux = platforms.path("linux").asBoolean(false);

            Integer recommendations = null;
            if (dataNode.has("recommendations")){
                recommendations = dataNode.path("recommendations").path("total").asInt();
            }

            JsonNode releaseDate = dataNode.path("release_date");
            String rawReleaseDate = releaseDate.path("date").asText("");
            boolean isComingSoon = releaseDate.path("coming_soon").asBoolean(false);
            LocalDate releaseDateParsed = parseSteamDate(rawReleaseDate, isComingSoon);

            String posterUrl = String.format("https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/%s/library_600x900.jpg", appId);

            return SteamGameDetailsDto.builder()
                    .name(dataNode.path("name").asText(null))
                    .type(dataNode.path("type").asText(null))
                    .description(dataNode.path("short_description").asText(null))
                    .recommendations(recommendations)
                    .windows(windows)
                    .mac(mac)
                    .linux(linux)
                    .isComingSoon(isComingSoon)
                    .releaseDateParsed(releaseDateParsed)
                    .headerImageUrl(posterUrl)
                    .build();
        } catch (IOException e) {
            logger.error("Failed to map Steam game details JSON: {}", e.getMessage(), e);
            return null;
        }
    }


    //=====HELPERS======

    private void extractIdsRecursively(JsonNode node, Set<Long> result) {
        if (node == null || node.isEmpty()) return;
        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                if (ID_KEYS.contains(entry.getKey())) {
                    JsonNode value = entry.getValue();
                    if (value.isNumber()) {
                        result.add(value.asLong());
                    } else if (entry.getValue().isTextual()) {
                        try {
                            result.add(Long.parseLong(entry.getValue().asText()));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
                extractIdsRecursively(entry.getValue(), result);
            });
        } else if (node.isArray()) {
            for (JsonNode element : node) {
                extractIdsRecursively(element, result);
            }
        }
    }

    private LocalDate parseSteamDate(String dateStr, boolean isComingSoon){
        if (isComingSoon || dateStr == null || dateStr.isBlank()){
            return null;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM, yyyy", Locale.ENGLISH);
            return LocalDate.parse(dateStr, formatter);
        } catch (Exception e) {
            logger.warn("Could not parse release date string: '{}'. Returning null.", dateStr);
            return null;
        }
    }
}
