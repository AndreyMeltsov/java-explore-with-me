package ru.practicum.ewm;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<Hit, Integer> {

    @Query("select new ru.practicum.ViewStatsDto(h.app, h.uri, count(h.ip)) " +
            "from Hit h " +
            "where h.timestamp between ?1 and ?2 " +
            "group by h.app, h.uri " +
            "order by count(h.ip) desc")
    List<ViewStatsDto> getAllIdStats(LocalDateTime from, LocalDateTime to);

    @Query("select new ru.practicum.ViewStatsDto(h.app, h.uri, count(distinct h.ip)) " +
            "from Hit h " +
            "where h.timestamp between ?1 and ?2 " +
            "group by h.app, h.uri " +
            "order by count(distinct h.ip) desc")
    List<ViewStatsDto> getUniqueIdStats(LocalDateTime from, LocalDateTime to);

    @Query("select new ru.practicum.ViewStatsDto(h.app, h.uri, count(h.ip)) " +
            "from Hit h " +
            "where h.timestamp between ?1 and ?2 " +
            "and h.uri in ?3 " +
            "group by h.app, h.uri " +
            "order by count(h.ip) desc")
    List<ViewStatsDto> getAllIdStatsByUris(LocalDateTime from, LocalDateTime to, String[] uris);

    @Query("select new ru.practicum.ViewStatsDto(h.app, h.uri, count(distinct h.ip)) " +
            "from Hit h " +
            "where h.timestamp between ?1 and ?2 " +
            "and h.uri in ?3 " +
            "group by h.app, h.uri " +
            "order by count(distinct h.ip) desc")
    List<ViewStatsDto> getUniqueIdStatsByUris(LocalDateTime from, LocalDateTime to, String[] uris);
}
