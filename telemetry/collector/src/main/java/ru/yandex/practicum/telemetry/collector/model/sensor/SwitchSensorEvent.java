package ru.yandex.practicum.telemetry.collector.model.sensor;

import jakarta.validation.constraints.NotNull;

public class SwitchSensorEvent extends SensorEvent {

    @NotNull
    private Boolean state;

    public SwitchSensorEvent() {
        super(SensorEventType.SWITCH_SENSOR_EVENT);
    }

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }
}
