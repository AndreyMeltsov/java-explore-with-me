package ru.practicum.ewm;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.HitDto;
import ru.practicum.ViewStatsDto;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
@Slf4j
public class StatsService {
    private final EntityManager entityManager;
    private final StatsRepository statsRepository;

    @Transactional
    public void createHit(HitDto hitDto) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Hit hit = Hit.builder()
                .app(hitDto.getApp())
                .ip(hitDto.getIp())
                .uri(hitDto.getUri())
                .timestamp(LocalDateTime.parse(hitDto.getTimestamp(), formatter))
                .build();
        hit = statsRepository.save(hit);
        log.info("Statistic was added: {}", hit);
    }

    public List<ViewStatsDto> getStats(LocalDateTime from, LocalDateTime to, String[] uris, boolean unique) {
        QHit hit = QHit.hit;
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        List<BooleanExpression> conditions = new ArrayList<>();

        conditions.add(hit.timestamp.between(from, to));
        if (uris != null) {
            conditions.add(hit.uri.in(uris));
        }
        BooleanExpression finalCondition = conditions.stream()
                .reduce(BooleanExpression::and)
                .get();

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
}
