package ru.yandex.practicum.telemetry.collector.mapper;

import com.google.protobuf.Timestamp;
import java.time.Instant;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioConditionProto;
import ru.yandex.practicum.telemetry.collector.model.hub.ActionType;
import ru.yandex.practicum.telemetry.collector.model.hub.ConditionOperation;
import ru.yandex.practicum.telemetry.collector.model.hub.ConditionType;
import ru.yandex.practicum.telemetry.collector.model.hub.DeviceAction;
import ru.yandex.practicum.telemetry.collector.model.hub.DeviceAddedEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.DeviceRemovedEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.DeviceType;
import ru.yandex.practicum.telemetry.collector.model.hub.HubEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.ScenarioAddedEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.ScenarioCondition;
import ru.yandex.practicum.telemetry.collector.model.hub.ScenarioRemovedEvent;

@Component
public class HubEventProtoMapper {

    public HubEvent map(HubEventProto source) {
        HubEvent event = switch (source.getPayloadCase()) {
            case DEVICE_ADDED -> mapDeviceAdded(source);
            case DEVICE_REMOVED -> mapDeviceRemoved(source);
            case SCENARIO_ADDED -> mapScenarioAdded(source);
            case SCENARIO_REMOVED -> mapScenarioRemoved(source);
            case PAYLOAD_NOT_SET -> throw new IllegalArgumentException("Hub event payload is required");
        };

        event.setHubId(source.getHubId());
        event.setTimestamp(toInstant(source.getTimestamp()));
        return event;
    }

    private DeviceAddedEvent mapDeviceAdded(HubEventProto source) {
        DeviceAddedEvent event = new DeviceAddedEvent();
        event.setId(source.getDeviceAdded().getId());
        event.setDeviceType(DeviceType.valueOf(source.getDeviceAdded().getType().name()));
        return event;
    }

    private DeviceRemovedEvent mapDeviceRemoved(HubEventProto source) {
        DeviceRemovedEvent event = new DeviceRemovedEvent();
        event.setId(source.getDeviceRemoved().getId());
        return event;
    }

    private ScenarioAddedEvent mapScenarioAdded(HubEventProto source) {
        ScenarioAddedEvent event = new ScenarioAddedEvent();
        event.setName(source.getScenarioAdded().getName());
        event.setConditions(source.getScenarioAdded().getConditionList().stream().map(this::mapCondition).toList());
        event.setActions(source.getScenarioAdded().getActionList().stream().map(this::mapAction).toList());
        return event;
    }

    private ScenarioRemovedEvent mapScenarioRemoved(HubEventProto source) {
        ScenarioRemovedEvent event = new ScenarioRemovedEvent();
        event.setName(source.getScenarioRemoved().getName());
        return event;
    }

    private ScenarioCondition mapCondition(ScenarioConditionProto source) {
        ScenarioCondition condition = new ScenarioCondition();
        condition.setSensorId(source.getSensorId());
        condition.setType(ConditionType.valueOf(source.getType().name()));
        condition.setOperation(ConditionOperation.valueOf(source.getOperation().name()));
        condition.setValue(switch (source.getValueCase()) {
            case BOOL_VALUE -> source.getBoolValue() ? 1 : 0;
            case INT_VALUE -> source.getIntValue();
            case VALUE_NOT_SET -> null;
        });
        return condition;
    }

    private DeviceAction mapAction(DeviceActionProto source) {
        DeviceAction action = new DeviceAction();
        action.setSensorId(source.getSensorId());
        action.setType(ActionType.valueOf(source.getType().name()));
        if (source.hasValue()) {
            action.setValue(source.getValue());
        }
        return action;
    }

    private Instant toInstant(Timestamp timestamp) {
        if (timestamp.getSeconds() == 0 && timestamp.getNanos() == 0) {
            return null;
        }
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }
}
