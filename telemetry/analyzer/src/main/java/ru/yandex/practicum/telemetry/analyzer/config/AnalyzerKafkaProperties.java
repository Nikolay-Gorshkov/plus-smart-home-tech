package ru.yandex.practicum.telemetry.analyzer.config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "analyzer.kafka")
public class AnalyzerKafkaProperties {

    private List<String> bootstrapServers = new ArrayList<>();
    private Duration pollTimeout = Duration.ofSeconds(5);
    private final Consumer hubConsumer = new Consumer();
    private final Consumer snapshotConsumer = new Consumer();
    private final Topics topics = new Topics();

    public List<String> getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(List<String> bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public Duration getPollTimeout() {
        return pollTimeout;
    }

    public void setPollTimeout(Duration pollTimeout) {
        this.pollTimeout = pollTimeout;
    }

    public Consumer getHubConsumer() {
        return hubConsumer;
    }

    public Consumer getSnapshotConsumer() {
        return snapshotConsumer;
    }

    public Topics getTopics() {
        return topics;
    }

    public static class Consumer {

        private String groupId;
        private String autoOffsetReset = "earliest";
        private boolean enableAutoCommit = false;
        private Integer maxPollRecords = 1;
        private Integer maxPartitionFetchBytes;
        private Map<String, String> properties = new HashMap<>();

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getAutoOffsetReset() {
            return autoOffsetReset;
        }

        public void setAutoOffsetReset(String autoOffsetReset) {
            this.autoOffsetReset = autoOffsetReset;
        }

        public boolean isEnableAutoCommit() {
            return enableAutoCommit;
        }

        public void setEnableAutoCommit(boolean enableAutoCommit) {
            this.enableAutoCommit = enableAutoCommit;
        }

        public Integer getMaxPollRecords() {
            return maxPollRecords;
        }

        public void setMaxPollRecords(Integer maxPollRecords) {
            this.maxPollRecords = maxPollRecords;
        }

        public Integer getMaxPartitionFetchBytes() {
            return maxPartitionFetchBytes;
        }

        public void setMaxPartitionFetchBytes(Integer maxPartitionFetchBytes) {
            this.maxPartitionFetchBytes = maxPartitionFetchBytes;
        }

        public Map<String, String> getProperties() {
            return properties;
        }

        public void setProperties(Map<String, String> properties) {
            this.properties = properties;
        }
    }

    public static class Topics {

        private String hubEvents = "telemetry.hubs.v1";
        private String snapshots = "telemetry.snapshots.v1";

        public String getHubEvents() {
            return hubEvents;
        }

        public void setHubEvents(String hubEvents) {
            this.hubEvents = hubEvents;
        }

        public String getSnapshots() {
            return snapshots;
        }

        public void setSnapshots(String snapshots) {
            this.snapshots = snapshots;
        }
    }
}
