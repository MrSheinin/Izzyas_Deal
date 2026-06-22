package by.Rsh.database.specification;

import by.Rsh.database.entity.GameEntity;
import by.Rsh.database.entity.GameMarketDataEntity;
import by.Rsh.database.entity.GenreEntity;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;

public class GameSpecs {

    public static Specification<GameEntity> isNotComingSoon(){
        return ((root, query, cb) -> cb.equal(root.get("isComingSoon"), false));
    }

    public static Specification<GameEntity> hasWindows(Boolean windows){
        return ((root, query, cb) -> {
            if (windows == null) return null;
            return cb.equal(root.get("windows"), windows);
        });
    }

    public static Specification<GameEntity> hasMac(Boolean mac){
        return ((root, query, cb) -> {
            if (mac == null) return null;
            return cb.equal(root.get("mac"), mac);
        });
    }

    public static Specification<GameEntity> hasLinux(Boolean linux){
        return ((root, query, cb) -> {
            if (linux == null) return null;
            return cb.equal(root.get("linux"), linux);
        });
    }

    public static Specification<GameEntity> hasMinPrice(Integer minPrice){
        return ((root, query, cb) -> {
            if (minPrice == null) return null;
            int priceInCents = minPrice * 100;

            Subquery<Long> subquery = query.subquery(Long.class);
            Root<GameMarketDataEntity> subRoot = subquery.from(GameMarketDataEntity.class);


            subquery.select(subRoot.get("appId"))
                    .where(
                            cb.equal(subRoot.get("appId"), root.get("appId")),
                            cb.greaterThanOrEqualTo(subRoot.get("finalPrice"), priceInCents)
                    );
            return cb.exists(subquery);
        });
    }

    public static Specification<GameEntity> hasMaxPrice(Integer maxPrice){
        return ((root, query, cb) -> {
            if (maxPrice == null) return null;
            int priceInCents = maxPrice * 100;

            Subquery<Long> subquery = query.subquery(Long.class);
            Root<GameMarketDataEntity> subRoot = subquery.from(GameMarketDataEntity.class);


            subquery.select(subRoot.get("appId"))
                    .where(
                            cb.equal(subRoot.get("appId"), root.get("appId")),
                            cb.lessThanOrEqualTo(subRoot.get("finalPrice"), priceInCents)
                    );
            return cb.exists(subquery);
        });
    }

    public static Specification<GameEntity> releasedAfterYear(Integer startYear){
        return ((root, query, cb) -> {
            if (startYear == null) return null;

            LocalDate startDate = LocalDate.of(startYear, 1, 1);

            return cb.greaterThanOrEqualTo(root.get("releaseDateParsed"), startDate);
        });
    }

    public static Specification<GameEntity> hasGenres(List<Long> genreIds){
        return ((root, query, cb) -> {
            if (genreIds == null || genreIds.isEmpty()) return null;

            Join<GameEntity, GenreEntity> genreJoin = root.join("genres");
            query.distinct(true);

            return genreJoin.get("genreId").in(genreIds);
        });
    }
}
