package ru.yandex.practicum.telemetry.collector.model.hub;

import jakarta.validation.constraints.NotBlank;

public class DeviceRemovedEvent extends HubEvent {

    @NotBlank
    private String id;

    public DeviceRemovedEvent() {
        super(HubEventType.DEVICE_REMOVED);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
