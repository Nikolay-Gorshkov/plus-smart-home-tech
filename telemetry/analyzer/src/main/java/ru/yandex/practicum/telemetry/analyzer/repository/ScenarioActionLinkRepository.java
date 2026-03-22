package ru.yandex.practicum.telemetry.analyzer.repository;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.telemetry.analyzer.entity.ScenarioActionLinkEntity;
import ru.yandex.practicum.telemetry.analyzer.entity.ScenarioActionLinkId;

public interface ScenarioActionLinkRepository extends JpaRepository<ScenarioActionLinkEntity, ScenarioActionLinkId> {

    @Query("""
            select link
            from ScenarioActionLinkEntity link
            join fetch link.sensor
            join fetch link.action
            join fetch link.scenario
            where link.scenario.id in :scenarioIds
            """)
    List<ScenarioActionLinkEntity> findAllByScenarioIds(@Param("scenarioIds") Collection<Long> scenarioIds);

    @Query("""
            select link
            from ScenarioActionLinkEntity link
            join fetch link.action
            join fetch link.scenario
            where link.scenario.id = :scenarioId
            """)
    List<ScenarioActionLinkEntity> findAllByScenarioId(@Param("scenarioId") Long scenarioId);

    @Query("""
            select link
            from ScenarioActionLinkEntity link
            join fetch link.action
            join fetch link.scenario
            where link.sensor.id = :sensorId
            """)
    List<ScenarioActionLinkEntity> findAllBySensorId(@Param("sensorId") String sensorId);
}
