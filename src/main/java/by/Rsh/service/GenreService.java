package by.Rsh.service;

import by.Rsh.database.repository.GenreRepository;
import by.Rsh.dto.dataDelivery.GenreDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreRepository genreRepository;

    @Cacheable("genres")
    public List<GenreDto> getAllGenres(){
        return genreRepository.findAll().stream()
                .map(genreEntity -> new GenreDto(genreEntity.getGenreId(), genreEntity.getName()))
                .collect(Collectors.toList());
    }

    @CacheEvict(value = "genres", allEntries = true)
    public void clearGenresCache() {
    }
}