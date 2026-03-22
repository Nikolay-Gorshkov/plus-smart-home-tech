package ru.yandex.practicum.telemetry.analyzer.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;

@Service
public class ScenarioEvaluator {

    private final ScenarioService scenarioService;
    private final HubRouterActionService hubRouterActionService;

    public ScenarioEvaluator(
            ScenarioService scenarioService,
            HubRouterActionService hubRouterActionService
    ) {
        this.scenarioService = scenarioService;
        this.hubRouterActionService = hubRouterActionService;
    }

    public void process(SensorsSnapshotAvro snapshot) {
        for (ScenarioDefinition scenario : scenarioService.getScenarios(snapshot.getHubId())) {
            if (matches(snapshot, scenario)) {
                hubRouterActionService.execute(snapshot, scenario);
            }
        }
    }

    private boolean matches(SensorsSnapshotAvro snapshot, ScenarioDefinition scenario) {
        return scenario.conditions().stream().allMatch(condition -> matches(snapshot, condition));
    }

    private boolean matches(SensorsSnapshotAvro snapshot, ScenarioDefinition.ConditionDefinition condition) {
        SensorStateAvro sensorState = snapshot.getSensorsState().get(condition.sensorId());
        if (sensorState == null) {
            return false;
        }

        Object data = sensorState.getData();
        return switch (condition.type()) {
            case MOTION -> compareBoolean(
                    data instanceof MotionSensorAvro motion ? motion.getMotion() : null,
                    condition.operation(),
                    condition.value()
            );
            case SWITCH -> compareBoolean(
                    data instanceof SwitchSensorAvro switchSensor ? switchSensor.getState() : null,
                    condition.operation(),
                    condition.value()
            );
            case LUMINOSITY -> compareInteger(
                    data instanceof LightSensorAvro light ? light.getLuminosity() : null,
                    condition.operation(),
                    condition.value()
            );
            case TEMPERATURE -> compareInteger(extractTemperature(data), condition.operation(), condition.value());
            case CO2LEVEL -> compareInteger(
                    data instanceof ClimateSensorAvro climate ? climate.getCo2Level() : null,
                    condition.operation(),
                    condition.value()
            );
            case HUMIDITY -> compareInteger(
                    data instanceof ClimateSensorAvro climate ? climate.getHumidity() : null,
                    condition.operation(),
                    condition.value()
            );
        };
    }

    private Integer extractTemperature(Object data) {
        if (data instanceof ClimateSensorAvro climate) {
            return climate.getTemperatureC();
        }
        if (data instanceof TemperatureSensorAvro temperature) {
            return temperature.getTemperatureC();
        }
        return null;
    }

    private boolean compareInteger(Integer actual, ConditionOperationAvro operation, Integer expected) {
        if (actual == null || expected == null) {
            return false;
        }

        return switch (operation) {
            case EQUALS -> actual.intValue() == expected.intValue();
            case GREATER_THAN -> actual > expected;
            case LOWER_THAN -> actual < expected;
        };
    }

    private boolean compareBoolean(Boolean actual, ConditionOperationAvro operation, Integer expected) {
        if (actual == null || expected == null || operation != ConditionOperationAvro.EQUALS) {
            return false;
        }

        return actual == (expected != 0);
    }
}
