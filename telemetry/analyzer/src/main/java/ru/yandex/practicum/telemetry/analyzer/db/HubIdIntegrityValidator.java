package ru.yandex.practicum.telemetry.analyzer.db;

import java.util.Objects;
import ru.yandex.practicum.telemetry.analyzer.entity.ScenarioEntity;
import ru.yandex.practicum.telemetry.analyzer.entity.SensorEntity;

public final class HubIdIntegrityValidator {

    private HubIdIntegrityValidator() {
    }

    public static void validate(ScenarioEntity scenario, SensorEntity sensor) {
        if (scenario == null || sensor == null) {
            return;
        }

        String scenarioHubId = scenario.getHubId();
        String sensorHubId = sensor.getHubId();
        if (scenarioHubId == null || sensorHubId == null || Objects.equals(scenarioHubId, sensorHubId)) {
            return;
        }

        throw new IllegalStateException(
                "Hub IDs do not match for scenario hub " + scenarioHubId + " and sensor " + sensor.getId()
        );
    }
}
