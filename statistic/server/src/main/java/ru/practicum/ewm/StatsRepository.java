package ru.practicum.ewm;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface StatsRepository extends JpaRepository<Hit, Integer>, QuerydslPredicateExecutor<Hit> {
}
