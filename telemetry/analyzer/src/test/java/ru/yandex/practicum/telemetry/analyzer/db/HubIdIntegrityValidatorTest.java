package ru.yandex.practicum.telemetry.analyzer.db;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.telemetry.analyzer.entity.ScenarioEntity;
import ru.yandex.practicum.telemetry.analyzer.entity.SensorEntity;

class HubIdIntegrityValidatorTest {

    @Test
    void allowsMatchingHubIds() {
        ScenarioEntity scenario = new ScenarioEntity("hub-1", "scenario-1");
        SensorEntity sensor = new SensorEntity("sensor-1", "hub-1");

        assertDoesNotThrow(() -> HubIdIntegrityValidator.validate(scenario, sensor));
    }

    @Test
    void rejectsDifferentHubIds() {
        ScenarioEntity scenario = new ScenarioEntity("hub-1", "scenario-1");
        SensorEntity sensor = new SensorEntity("sensor-1", "hub-2");

        assertThrows(IllegalStateException.class, () -> HubIdIntegrityValidator.validate(scenario, sensor));
    }
}
