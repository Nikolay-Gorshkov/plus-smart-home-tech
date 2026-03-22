package ru.yandex.practicum.telemetry.analyzer.service;

import java.util.List;
import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;

public record ScenarioDefinition(
        String name,
        List<ConditionDefinition> conditions,
        List<ActionDefinition> actions
) {

    public record ConditionDefinition(
            String sensorId,
            ConditionTypeAvro type,
            ConditionOperationAvro operation,
            Integer value
    ) {
    }

    public record ActionDefinition(
            String sensorId,
            ActionTypeAvro type,
            Integer value
    ) {
    }
}
