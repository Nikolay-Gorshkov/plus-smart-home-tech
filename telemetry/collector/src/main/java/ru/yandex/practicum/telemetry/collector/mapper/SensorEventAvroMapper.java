package ru.yandex.practicum.telemetry.collector.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;
import ru.yandex.practicum.telemetry.collector.model.sensor.ClimateSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.LightSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.MotionSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.SensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.SwitchSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.TemperatureSensorEvent;

@Component
public class SensorEventAvroMapper {

    public SensorEventAvro map(SensorEvent event) {
        return SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setPayload(mapPayload(event))
                .build();
    }

    private Object mapPayload(SensorEvent event) {
        return switch (event.getType()) {
            case CLIMATE_SENSOR_EVENT -> mapClimate((ClimateSensorEvent) event);
            case LIGHT_SENSOR_EVENT -> mapLight((LightSensorEvent) event);
            case MOTION_SENSOR_EVENT -> mapMotion((MotionSensorEvent) event);
            case SWITCH_SENSOR_EVENT -> mapSwitch((SwitchSensorEvent) event);
            case TEMPERATURE_SENSOR_EVENT -> mapTemperature((TemperatureSensorEvent) event);
        };
    }

    private ClimateSensorAvro mapClimate(ClimateSensorEvent event) {
        return ClimateSensorAvro.newBuilder()
                .setTemperatureC(requiredInteger(event.getTemperatureC(), "temperatureC"))
                .setHumidity(requiredInteger(event.getHumidity(), "humidity"))
                .setCo2Level(requiredInteger(event.getCo2Level(), "co2Level"))
                .build();
    }

    private LightSensorAvro mapLight(LightSensorEvent event) {
        return LightSensorAvro.newBuilder()
                .setLinkQuality(defaultInteger(event.getLinkQuality()))
                .setLuminosity(defaultInteger(event.getLuminosity()))
                .build();
    }

    private MotionSensorAvro mapMotion(MotionSensorEvent event) {
        return MotionSensorAvro.newBuilder()
                .setLinkQuality(requiredInteger(event.getLinkQuality(), "linkQuality"))
                .setMotion(requiredBoolean(event.getMotion(), "motion"))
                .setVoltage(requiredInteger(event.getVoltage(), "voltage"))
                .build();
    }

    private SwitchSensorAvro mapSwitch(SwitchSensorEvent event) {
        return SwitchSensorAvro.newBuilder()
                .setState(requiredBoolean(event.getState(), "state"))
                .build();
    }

    private TemperatureSensorAvro mapTemperature(TemperatureSensorEvent event) {
        return TemperatureSensorAvro.newBuilder()
                .setTemperatureC(requiredInteger(event.getTemperatureC(), "temperatureC"))
                .setTemperatureF(requiredInteger(event.getTemperatureF(), "temperatureF"))
                .build();
    }

    private int requiredInteger(Integer value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException("Missing required field: " + fieldName);
        }
        return value;
    }

    private boolean requiredBoolean(Boolean value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException("Missing required field: " + fieldName);
        }
        return value;
    }

    private int defaultInteger(Integer value) {
        return value == null ? 0 : value;
    }
}
