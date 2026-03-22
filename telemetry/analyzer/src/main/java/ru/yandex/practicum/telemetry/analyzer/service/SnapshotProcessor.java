package ru.yandex.practicum.telemetry.analyzer.service;

import jakarta.annotation.PreDestroy;
import java.util.List;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.telemetry.analyzer.config.AnalyzerKafkaProperties;

@Component
public class SnapshotProcessor {

    private static final Logger log = LoggerFactory.getLogger(SnapshotProcessor.class);

    private final Consumer<String, byte[]> kafkaConsumer;
    private final AnalyzerKafkaProperties kafkaProperties;
    private final SensorsSnapshotAvroDeserializer snapshotDeserializer;
    private final ScenarioEvaluator scenarioEvaluator;
    private volatile boolean running = true;

    public SnapshotProcessor(
            @Qualifier("snapshotKafkaConsumer") Consumer<String, byte[]> kafkaConsumer,
            AnalyzerKafkaProperties kafkaProperties,
            SensorsSnapshotAvroDeserializer snapshotDeserializer,
            ScenarioEvaluator scenarioEvaluator
    ) {
        this.kafkaConsumer = kafkaConsumer;
        this.kafkaProperties = kafkaProperties;
        this.snapshotDeserializer = snapshotDeserializer;
        this.scenarioEvaluator = scenarioEvaluator;
    }

    public void start() {
        kafkaConsumer.subscribe(List.of(kafkaProperties.getTopics().getSnapshots()));
        log.info("Analyzer snapshot processor subscribed to topic {}", kafkaProperties.getTopics().getSnapshots());

        try {
            while (running) {
                ConsumerRecords<String, byte[]> records = kafkaConsumer.poll(kafkaProperties.getPollTimeout());
                if (records.isEmpty()) {
                    continue;
                }

                process(records);
            }
        } catch (WakeupException exception) {
            if (running) {
                throw exception;
            }
        } finally {
            kafkaConsumer.close();
            log.info("Analyzer snapshot consumer closed");
        }
    }

    @PreDestroy
    public void stop() {
        running = false;
        kafkaConsumer.wakeup();
    }

    private void process(ConsumerRecords<String, byte[]> records) {
        for (ConsumerRecord<String, byte[]> record : records) {
            SensorsSnapshotAvro snapshot = snapshotDeserializer.deserialize(record.topic(), record.value());
            scenarioEvaluator.process(snapshot);
        }

        kafkaConsumer.commitSync();
    }
}
