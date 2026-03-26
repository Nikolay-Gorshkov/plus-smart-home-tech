package ru.yandex.practicum.telemetry.collector.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "collector.kafka.topics")
public class KafkaTopicsProperties {

    private String sensorEvents;
    private String hubEvents;

    public String getSensorEvents() {
        return sensorEvents;
    }

    public void setSensorEvents(String sensorEvents) {
        this.sensorEvents = sensorEvents;
    }

    public String getHubEvents() {
        return hubEvents;
    }

    public void setHubEvents(String hubEvents) {
        this.hubEvents = hubEvents;
    }
}
