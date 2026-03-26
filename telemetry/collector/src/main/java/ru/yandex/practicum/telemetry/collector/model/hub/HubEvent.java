package ru.yandex.practicum.telemetry.collector.model.hub;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
public abstract class HubEvent {

    @NotBlank
    private String hubId;

    @NotNull
    private Instant timestamp = Instant.now();

    @NotNull
    private HubEventType type;

    protected HubEvent() {
    }

    protected HubEvent(HubEventType type) {
        this.type = type;
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

    public HubEventType getType() {
        return type;
    }

    public void setType(HubEventType type) {
        this.type = type;
    }
}
