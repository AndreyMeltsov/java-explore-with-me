package ru.practicum.statservice;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.statdto.HitDto;
import ru.practicum.statdto.ViewStatsDto;
import ru.practicum.statservice.exseptions.DateTimeDecodingException;
import ru.practicum.statservice.exseptions.IllegalArgumentExcepton;
import ru.practicum.statservice.exseptions.NotFoundException;

import javax.persistence.EntityManager;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
@Slf4j
public class StatsService {
    private final StatsRepository statsRepository;
    private final EntityManager entityManager;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Transactional
    public void createHit(HitDto hitDto) {
        Hit hit = Hit.builder()
                .app(hitDto.getApp())
                .ip(hitDto.getIp())
                .uri(hitDto.getUri())
                .timestamp(LocalDateTime.parse(hitDto.getTimestamp(), FORMATTER))
                .build();
        log.info("Statistic was added: {}", statsRepository.save(hit));
    }

    public List<ViewStatsDto> getStats(String start, String end, String[] uris, boolean unique) {
        LocalDateTime from;
        LocalDateTime to;
        try {
            from = LocalDateTime.parse(decodeValue(start), FORMATTER);
            to = LocalDateTime.parse(decodeValue(end), FORMATTER);
        } catch (UnsupportedEncodingException e) {
            throw new DateTimeDecodingException("Can't decode received value");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentExcepton("Start date can't be later end date");
        }

        QHit hit = QHit.hit;
        List<BooleanExpression> conditions = new ArrayList<>();
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        conditions.add(hit.timestamp.goe(from).and(hit.timestamp.loe(to)));
        if (uris != null) {
            conditions.add(hit.uri.in(uris));
        }
        BooleanExpression finalCondition = conditions.stream()
                .reduce(BooleanExpression::and)
                .orElseThrow(() -> new NotFoundException("At least one condition should be assigned"));

        List<ViewStatsDto> stats = queryFactory
                .select(Projections.constructor(ViewStatsDto.class, hit.app, hit.uri, makeUniqueCondition(unique)))
                .from(hit)
                .where(finalCondition)
                .groupBy(hit.app, hit.uri)
                .orderBy(makeUniqueCondition(unique).desc())
                .fetch();

        log.info("{} lines of statistic were found between {} and {} for uris {}, ip unique={}",
                stats.size(), from, to, uris, unique);
        return stats;
    }

    private NumberExpression<Long> makeUniqueCondition(boolean unique) {
        if (unique) {
            return QHit.hit.ip.countDistinct();
        } else {
            return QHit.hit.ip.count();
        }
    }

    private String decodeValue(String value) throws UnsupportedEncodingException {
        return URLDecoder.decode(value, StandardCharsets.UTF_8.toString());
    }
}
