package ru.yandex.practicum.telemetry.analyzer.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;
import ru.yandex.practicum.telemetry.analyzer.entity.ActionEntity;
import ru.yandex.practicum.telemetry.analyzer.entity.ConditionEntity;
import ru.yandex.practicum.telemetry.analyzer.entity.ScenarioActionLinkEntity;
import ru.yandex.practicum.telemetry.analyzer.entity.ScenarioConditionLinkEntity;
import ru.yandex.practicum.telemetry.analyzer.entity.ScenarioEntity;
import ru.yandex.practicum.telemetry.analyzer.entity.SensorEntity;
import ru.yandex.practicum.telemetry.analyzer.repository.ActionRepository;
import ru.yandex.practicum.telemetry.analyzer.repository.ConditionRepository;
import ru.yandex.practicum.telemetry.analyzer.repository.ScenarioActionLinkRepository;
import ru.yandex.practicum.telemetry.analyzer.repository.ScenarioConditionLinkRepository;
import ru.yandex.practicum.telemetry.analyzer.repository.ScenarioRepository;
import ru.yandex.practicum.telemetry.analyzer.repository.SensorRepository;

@Service
public class ScenarioService {

    private final ScenarioRepository scenarioRepository;
    private final SensorRepository sensorRepository;
    private final ConditionRepository conditionRepository;
    private final ActionRepository actionRepository;
    private final ScenarioConditionLinkRepository scenarioConditionLinkRepository;
    private final ScenarioActionLinkRepository scenarioActionLinkRepository;

    public ScenarioService(
            ScenarioRepository scenarioRepository,
            SensorRepository sensorRepository,
            ConditionRepository conditionRepository,
            ActionRepository actionRepository,
            ScenarioConditionLinkRepository scenarioConditionLinkRepository,
            ScenarioActionLinkRepository scenarioActionLinkRepository
    ) {
        this.scenarioRepository = scenarioRepository;
        this.sensorRepository = sensorRepository;
        this.conditionRepository = conditionRepository;
        this.actionRepository = actionRepository;
        this.scenarioConditionLinkRepository = scenarioConditionLinkRepository;
        this.scenarioActionLinkRepository = scenarioActionLinkRepository;
    }

    @Transactional
    public void handleHubEvent(HubEventAvro event) {
        Object payload = Objects.requireNonNull(event.getPayload(), "Hub event payload is required");

        switch (payload) {
            case DeviceAddedEventAvro deviceAdded -> saveSensor(event.getHubId(), deviceAdded.getId());
            case DeviceRemovedEventAvro deviceRemoved -> removeSensor(event.getHubId(), deviceRemoved.getId());
            case ScenarioAddedEventAvro scenarioAdded -> saveScenario(event.getHubId(), scenarioAdded);
            case ScenarioRemovedEventAvro scenarioRemoved -> removeScenario(event.getHubId(), scenarioRemoved.getName());
            default -> throw new IllegalArgumentException("Unsupported hub event payload type: " + payload.getClass().getName());
        }
    }

    @Transactional(readOnly = true)
    public List<ScenarioDefinition> getScenarios(String hubId) {
        List<ScenarioEntity> scenarios = scenarioRepository.findByHubId(hubId);
        if (scenarios.isEmpty()) {
            return List.of();
        }

        List<Long> scenarioIds = scenarios.stream()
                .map(ScenarioEntity::getId)
                .toList();

        Map<Long, List<ScenarioConditionLinkEntity>> conditionsByScenario = scenarioConditionLinkRepository
                .findAllByScenarioIds(scenarioIds)
                .stream()
                .collect(Collectors.groupingBy(link -> link.getScenario().getId()));

        Map<Long, List<ScenarioActionLinkEntity>> actionsByScenario = scenarioActionLinkRepository
                .findAllByScenarioIds(scenarioIds)
                .stream()
                .collect(Collectors.groupingBy(link -> link.getScenario().getId()));

        return scenarios.stream()
                .map(scenario -> new ScenarioDefinition(
                        scenario.getName(),
                        conditionsByScenario.getOrDefault(scenario.getId(), List.of()).stream()
                                .map(link -> new ScenarioDefinition.ConditionDefinition(
                                        link.getSensor().getId(),
                                        link.getCondition().getType(),
                                        link.getCondition().getOperation(),
                                        link.getCondition().getValue()
                                ))
                                .toList(),
                        actionsByScenario.getOrDefault(scenario.getId(), List.of()).stream()
                                .map(link -> new ScenarioDefinition.ActionDefinition(
                                        link.getSensor().getId(),
                                        link.getAction().getType(),
                                        link.getAction().getValue()
                                ))
                                .toList()
                ))
                .toList();
    }

    private void saveSensor(String hubId, String sensorId) {
        sensorRepository.save(new SensorEntity(sensorId, hubId));
    }

    private void saveScenario(String hubId, ScenarioAddedEventAvro scenarioAddedEvent) {
        removeScenario(hubId, scenarioAddedEvent.getName());

        ensureSensorsExist(hubId, collectSensorIds(scenarioAddedEvent));

        ScenarioEntity scenario = scenarioRepository.save(new ScenarioEntity(hubId, scenarioAddedEvent.getName()));
        Map<String, SensorEntity> sensorsById = sensorRepository.findAllById(collectSensorIds(scenarioAddedEvent))
                .stream()
                .collect(Collectors.toMap(SensorEntity::getId, Function.identity()));

        List<ScenarioConditionLinkEntity> conditionLinks = new ArrayList<>();
        for (ScenarioConditionAvro conditionAvro : scenarioAddedEvent.getConditions()) {
            SensorEntity sensor = requireSensor(sensorsById, conditionAvro.getSensorId(), hubId);
            ConditionEntity condition = conditionRepository.save(new ConditionEntity(
                    Objects.requireNonNull(conditionAvro.getType(), "Scenario condition type is required"),
                    Objects.requireNonNull(conditionAvro.getOperation(), "Scenario condition operation is required"),
                    mapConditionValue(conditionAvro.getValue())
            ));
            conditionLinks.add(new ScenarioConditionLinkEntity(scenario, sensor, condition));
        }

        List<ScenarioActionLinkEntity> actionLinks = new ArrayList<>();
        for (DeviceActionAvro actionAvro : scenarioAddedEvent.getActions()) {
            SensorEntity sensor = requireSensor(sensorsById, actionAvro.getSensorId(), hubId);
            ActionEntity action = actionRepository.save(new ActionEntity(
                    Objects.requireNonNull(actionAvro.getType(), "Scenario action type is required"),
                    actionAvro.getValue()
            ));
            actionLinks.add(new ScenarioActionLinkEntity(scenario, sensor, action));
        }

        scenarioConditionLinkRepository.saveAll(conditionLinks);
        scenarioActionLinkRepository.saveAll(actionLinks);
    }

    private void ensureSensorsExist(String hubId, Collection<String> sensorIds) {
        if (sensorIds.isEmpty()) {
            return;
        }

        Map<String, SensorEntity> existingSensors = sensorRepository.findAllById(sensorIds)
                .stream()
                .collect(Collectors.toMap(SensorEntity::getId, Function.identity()));

        List<SensorEntity> sensorsToSave = new ArrayList<>();
        for (String sensorId : sensorIds) {
            SensorEntity existingSensor = existingSensors.get(sensorId);
            if (existingSensor == null || !hubId.equals(existingSensor.getHubId())) {
                sensorsToSave.add(new SensorEntity(sensorId, hubId));
            }
        }

        if (!sensorsToSave.isEmpty()) {
            sensorRepository.saveAll(sensorsToSave);
        }
    }

    private Set<String> collectSensorIds(ScenarioAddedEventAvro scenarioAddedEvent) {
        Set<String> sensorIds = new LinkedHashSet<>();
        scenarioAddedEvent.getConditions().stream()
                .map(ScenarioConditionAvro::getSensorId)
                .forEach(sensorIds::add);
        scenarioAddedEvent.getActions().stream()
                .map(DeviceActionAvro::getSensorId)
                .forEach(sensorIds::add);
        return sensorIds;
    }

    private SensorEntity requireSensor(Map<String, SensorEntity> sensorsById, String sensorId, String hubId) {
        SensorEntity sensor = sensorsById.get(sensorId);
        if (sensor == null) {
            throw new IllegalStateException("Sensor " + sensorId + " is not registered for hub " + hubId);
        }
        return sensor;
    }

    private Integer mapConditionValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer integerValue) {
            return integerValue;
        }
        if (value instanceof Boolean booleanValue) {
            return booleanValue ? 1 : 0;
        }
        throw new IllegalArgumentException("Unsupported scenario condition value type: " + value.getClass().getName());
    }

    @Transactional
    public void removeScenario(String hubId, String scenarioName) {
        scenarioRepository.findByHubIdAndName(hubId, scenarioName).ifPresent(this::removeScenario);
    }

    @Transactional
    public void removeSensor(String hubId, String sensorId) {
        SensorEntity sensor = sensorRepository.findByIdAndHubId(sensorId, hubId).orElse(null);
        if (sensor == null) {
            return;
        }

        List<ScenarioConditionLinkEntity> conditionLinks = scenarioConditionLinkRepository.findAllBySensorId(sensorId);
        if (!conditionLinks.isEmpty()) {
            scenarioConditionLinkRepository.deleteAll(conditionLinks);
            conditionRepository.deleteAll(conditionLinks.stream()
                    .map(ScenarioConditionLinkEntity::getCondition)
                    .toList());
        }

        List<ScenarioActionLinkEntity> actionLinks = scenarioActionLinkRepository.findAllBySensorId(sensorId);
        if (!actionLinks.isEmpty()) {
            scenarioActionLinkRepository.deleteAll(actionLinks);
            actionRepository.deleteAll(actionLinks.stream()
                    .map(ScenarioActionLinkEntity::getAction)
                    .toList());
        }

        sensorRepository.delete(sensor);
    }

    private void removeScenario(ScenarioEntity scenario) {
        List<ScenarioConditionLinkEntity> conditionLinks = scenarioConditionLinkRepository.findAllByScenarioId(scenario.getId());
        if (!conditionLinks.isEmpty()) {
            scenarioConditionLinkRepository.deleteAll(conditionLinks);
            conditionRepository.deleteAll(conditionLinks.stream()
                    .map(ScenarioConditionLinkEntity::getCondition)
                    .toList());
        }

        List<ScenarioActionLinkEntity> actionLinks = scenarioActionLinkRepository.findAllByScenarioId(scenario.getId());
        if (!actionLinks.isEmpty()) {
            scenarioActionLinkRepository.deleteAll(actionLinks);
            actionRepository.deleteAll(actionLinks.stream()
                    .map(ScenarioActionLinkEntity::getAction)
                    .toList());
        }

        scenarioRepository.delete(scenario);
    }
}
