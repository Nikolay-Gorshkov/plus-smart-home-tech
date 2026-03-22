package ru.yandex.practicum.telemetry.analyzer.repository;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.telemetry.analyzer.entity.ScenarioConditionLinkEntity;
import ru.yandex.practicum.telemetry.analyzer.entity.ScenarioConditionLinkId;

public interface ScenarioConditionLinkRepository extends JpaRepository<ScenarioConditionLinkEntity, ScenarioConditionLinkId> {

    @Query("""
            select link
            from ScenarioConditionLinkEntity link
            join fetch link.sensor
            join fetch link.condition
            join fetch link.scenario
            where link.scenario.id in :scenarioIds
            """)
    List<ScenarioConditionLinkEntity> findAllByScenarioIds(@Param("scenarioIds") Collection<Long> scenarioIds);

    @Query("""
            select link
            from ScenarioConditionLinkEntity link
            join fetch link.condition
            join fetch link.scenario
            where link.scenario.id = :scenarioId
            """)
    List<ScenarioConditionLinkEntity> findAllByScenarioId(@Param("scenarioId") Long scenarioId);

    @Query("""
            select link
            from ScenarioConditionLinkEntity link
            join fetch link.condition
            join fetch link.scenario
            where link.sensor.id = :sensorId
            """)
    List<ScenarioConditionLinkEntity> findAllBySensorId(@Param("sensorId") String sensorId);
}
