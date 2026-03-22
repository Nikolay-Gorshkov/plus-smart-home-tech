package ru.yandex.practicum.telemetry.aggregator.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;

@Service
public class SnapshotService {

    private final Map<String, SensorsSnapshotAvro> snapshots = new HashMap<>();

    public Optional<SensorsSnapshotAvro> update(SensorEventAvro event) {
        SensorsSnapshotAvro snapshot = snapshots.computeIfAbsent(
                event.getHubId(),
                ignored -> SensorsSnapshotAvro.newBuilder()
                        .setHubId(event.getHubId())
                        .setTimestamp(event.getTimestamp())
                        .setSensorsState(new HashMap<>())
                        .build()
        );

        Map<String, SensorStateAvro> sensorsState = snapshot.getSensorsState();
        SensorStateAvro currentState = sensorsState.get(event.getId());
        if (!shouldUpdate(currentState, event)) {
            return Optional.empty();
        }

        SensorStateAvro newState = SensorStateAvro.newBuilder()
                .setTimestamp(event.getTimestamp())
                .setData(copyPayload(event.getPayload()))
                .build();

        sensorsState.put(event.getId(), newState);
        snapshot.setTimestamp(newState.getTimestamp());
        return Optional.of(snapshot);
    }

    private boolean shouldUpdate(SensorStateAvro currentState, SensorEventAvro event) {
        if (currentState == null) {
            return true;
        }

        if (hasSameData(currentState, event)) {
            return false;
        }

        Instant storedTimestamp = currentState.getTimestamp().truncatedTo(ChronoUnit.MILLIS);
        Instant eventTimestamp = event.getTimestamp().truncatedTo(ChronoUnit.MILLIS);
        return !storedTimestamp.isAfter(eventTimestamp);
    }

    private boolean hasSameData(SensorStateAvro currentState, SensorEventAvro event) {
        Object currentData = Objects.requireNonNull(currentState.getData(), "Current sensor state data is required");
        Object eventPayload = Objects.requireNonNull(event.getPayload(), "Sensor event payload is required");

        return switch (eventPayload) {
            case MotionSensorAvro motion when currentData instanceof MotionSensorAvro currentMotion ->
                    currentMotion.getMotion() == motion.getMotion()
                            && currentMotion.getLinkQuality() == motion.getLinkQuality()
                            && currentMotion.getVoltage() == motion.getVoltage();
            case LightSensorAvro light when currentData instanceof LightSensorAvro currentLight ->
                    currentLight.getLuminosity() == light.getLuminosity()
                            && currentLight.getLinkQuality() == light.getLinkQuality();
            case ClimateSensorAvro climate when currentData instanceof ClimateSensorAvro currentClimate ->
                    currentClimate.getTemperatureC() == climate.getTemperatureC()
                            && currentClimate.getHumidity() == climate.getHumidity()
                            && currentClimate.getCo2Level() == climate.getCo2Level();
            case SwitchSensorAvro switchSensor when currentData instanceof SwitchSensorAvro currentSwitch ->
                    currentSwitch.getState() == switchSensor.getState();
            case TemperatureSensorAvro temperature when currentData instanceof TemperatureSensorAvro currentTemperature ->
                    currentTemperature.getTemperatureC() == temperature.getTemperatureC()
                            && currentTemperature.getTemperatureF() == temperature.getTemperatureF();
            default -> throw new IllegalArgumentException(
                    "Unsupported sensor payload pair: "
                            + currentData.getClass().getName()
                            + " and "
                            + eventPayload.getClass().getName()
            );
        };
    }

    private Object copyPayload(Object payload) {
        return switch (Objects.requireNonNull(payload, "Sensor payload is required")) {
            case MotionSensorAvro motion -> MotionSensorAvro.newBuilder()
                    .setMotion(motion.getMotion())
                    .setLinkQuality(motion.getLinkQuality())
                    .setVoltage(motion.getVoltage())
                    .build();
            case LightSensorAvro light -> LightSensorAvro.newBuilder()
                    .setLuminosity(light.getLuminosity())
                    .setLinkQuality(light.getLinkQuality())
                    .build();
            case ClimateSensorAvro climate -> ClimateSensorAvro.newBuilder()
                    .setTemperatureC(climate.getTemperatureC())
                    .setHumidity(climate.getHumidity())
                    .setCo2Level(climate.getCo2Level())
                    .build();
            case SwitchSensorAvro switchSensor -> SwitchSensorAvro.newBuilder()
                    .setState(switchSensor.getState())
                    .build();
            case TemperatureSensorAvro temperature -> TemperatureSensorAvro.newBuilder()
                    .setTemperatureC(temperature.getTemperatureC())
                    .setTemperatureF(temperature.getTemperatureF())
                    .build();
            default -> throw new IllegalArgumentException("Unsupported sensor payload type: " + payload.getClass().getName());
        };
    }
}
