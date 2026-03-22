package ru.yandex.practicum.telemetry.collector.mapper;

import com.google.protobuf.Timestamp;
import java.time.Instant;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.ClimateSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.LightSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.MotionSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SwitchSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.TemperatureSensorProto;
import ru.yandex.practicum.telemetry.collector.model.sensor.ClimateSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.LightSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.MotionSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.SensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.SwitchSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.TemperatureSensorEvent;

@Component
public class SensorEventProtoMapper {

    public SensorEvent map(SensorEventProto source) {
        SensorEvent event = switch (source.getPayloadCase()) {
            case MOTION_SENSOR -> mapMotion(source.getMotionSensor());
            case TEMPERATURE_SENSOR -> mapTemperature(source.getTemperatureSensor());
            case LIGHT_SENSOR -> mapLight(source.getLightSensor());
            case CLIMATE_SENSOR -> mapClimate(source.getClimateSensor());
            case SWITCH_SENSOR -> mapSwitch(source.getSwitchSensor());
            case PAYLOAD_NOT_SET -> throw new IllegalArgumentException("Sensor event payload is required");
        };

        event.setId(source.getId());
        event.setHubId(source.getHubId());
        event.setTimestamp(toInstant(source.getTimestamp()));
        return event;
    }

    private MotionSensorEvent mapMotion(MotionSensorProto source) {
        MotionSensorEvent event = new MotionSensorEvent();
        event.setLinkQuality(source.getLinkQuality());
        event.setMotion(source.getMotion());
        event.setVoltage(source.getVoltage());
        return event;
    }

    private TemperatureSensorEvent mapTemperature(TemperatureSensorProto source) {
        TemperatureSensorEvent event = new TemperatureSensorEvent();
        event.setTemperatureC(source.getTemperatureC());
        event.setTemperatureF(source.getTemperatureF());
        return event;
    }

    private LightSensorEvent mapLight(LightSensorProto source) {
        LightSensorEvent event = new LightSensorEvent();
        event.setLinkQuality(source.getLinkQuality());
        event.setLuminosity(source.getLuminosity());
        return event;
    }

    private ClimateSensorEvent mapClimate(ClimateSensorProto source) {
        ClimateSensorEvent event = new ClimateSensorEvent();
        event.setTemperatureC(source.getTemperatureC());
        event.setHumidity(source.getHumidity());
        event.setCo2Level(source.getCo2Level());
        return event;
    }

    private SwitchSensorEvent mapSwitch(SwitchSensorProto source) {
        SwitchSensorEvent event = new SwitchSensorEvent();
        event.setState(source.getState());
        return event;
    }

    private Instant toInstant(Timestamp timestamp) {
        if (timestamp.getSeconds() == 0 && timestamp.getNanos() == 0) {
            return null;
        }
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }
}
