package ru.yandex.practicum.telemetry.collector.model.sensor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
public abstract class SensorEvent {

    @NotBlank
    private String id;

    @NotBlank
    private String hubId;

    @NotNull
    private Instant timestamp = Instant.now();

    @NotNull
    private SensorEventType type;

    protected SensorEvent() {
    }

    protected SensorEvent(SensorEventType type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHubId() {
        return hubId;
    }

    public void setHubId(String hubId) {
        this.hubId = hubId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        if (timestamp != null) {
            this.timestamp = timestamp;
        }
    }

    public SensorEventType getType() {
        return type;
    }

    public void setType(SensorEventType type) {
        this.type = type;
    }
}
