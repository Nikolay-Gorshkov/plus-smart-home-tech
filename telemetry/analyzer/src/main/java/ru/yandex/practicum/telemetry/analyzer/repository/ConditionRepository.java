package ru.yandex.practicum.telemetry.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.telemetry.analyzer.entity.ConditionEntity;

public interface ConditionRepository extends JpaRepository<ConditionEntity, Long> {
}
