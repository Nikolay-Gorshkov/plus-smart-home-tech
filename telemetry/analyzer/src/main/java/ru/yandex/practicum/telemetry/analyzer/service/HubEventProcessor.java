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
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.telemetry.analyzer.config.AnalyzerKafkaProperties;

@Component
public class HubEventProcessor implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(HubEventProcessor.class);

    private final Consumer<String, byte[]> kafkaConsumer;
    private final AnalyzerKafkaProperties kafkaProperties;
    private final HubEventAvroDeserializer hubEventDeserializer;
    private final ScenarioService scenarioService;
    private volatile boolean running = true;

    public HubEventProcessor(
            @Qualifier("hubEventKafkaConsumer") Consumer<String, byte[]> kafkaConsumer,
            AnalyzerKafkaProperties kafkaProperties,
            HubEventAvroDeserializer hubEventDeserializer,
            ScenarioService scenarioService
    ) {
        this.kafkaConsumer = kafkaConsumer;
        this.kafkaProperties = kafkaProperties;
        this.hubEventDeserializer = hubEventDeserializer;
        this.scenarioService = scenarioService;
    }

    @Override
    public void run() {
        kafkaConsumer.subscribe(List.of(kafkaProperties.getTopics().getHubEvents()));
        log.info("Analyzer hub event processor subscribed to topic {}", kafkaProperties.getTopics().getHubEvents());

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
            log.info("Analyzer hub event consumer closed");
        }
    }

    @PreDestroy
    public void stop() {
        running = false;
        kafkaConsumer.wakeup();
    }

    private void process(ConsumerRecords<String, byte[]> records) {
        for (ConsumerRecord<String, byte[]> record : records) {
            HubEventAvro event = hubEventDeserializer.deserialize(record.topic(), record.value());
            scenarioService.handleHubEvent(event);
        }

        kafkaConsumer.commitSync();
    }
}
