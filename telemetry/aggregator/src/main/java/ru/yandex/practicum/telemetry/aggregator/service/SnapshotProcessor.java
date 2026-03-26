package ru.yandex.practicum.telemetry.aggregator.service;

import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.telemetry.aggregator.config.AggregatorKafkaProperties;
import ru.yandex.practicum.telemetry.aggregator.exception.KafkaProcessingException;

@Component
public class SnapshotProcessor {

    private static final Logger log = LoggerFactory.getLogger(SnapshotProcessor.class);

    private final Consumer<String, byte[]> kafkaConsumer;
    private final Producer<String, SpecificRecordBase> kafkaProducer;
    private final AggregatorKafkaProperties kafkaProperties;
    private final SensorEventAvroDeserializer sensorEventDeserializer;
    private final SnapshotService snapshotService;
    private volatile boolean running = true;

    public SnapshotProcessor(
            Consumer<String, byte[]> kafkaConsumer,
            Producer<String, SpecificRecordBase> kafkaProducer,
            AggregatorKafkaProperties kafkaProperties,
            SensorEventAvroDeserializer sensorEventDeserializer,
            SnapshotService snapshotService
    ) {
        this.kafkaConsumer = kafkaConsumer;
        this.kafkaProducer = kafkaProducer;
        this.kafkaProperties = kafkaProperties;
        this.sensorEventDeserializer = sensorEventDeserializer;
        this.snapshotService = snapshotService;
    }

    public void start() {
        kafkaConsumer.subscribe(List.of(kafkaProperties.getTopics().getSensorEvents()));
        log.info("Aggregator subscribed to topic {}", kafkaProperties.getTopics().getSensorEvents());

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
            log.info("Aggregator consumer closed");
        }
    }

    @PreDestroy
    public void stop() {
        running = false;
        kafkaConsumer.wakeup();
    }

    private void process(ConsumerRecords<String, byte[]> records) {
        for (ConsumerRecord<String, byte[]> record : records) {
            SensorEventAvro event = sensorEventDeserializer.deserialize(record.topic(), record.value());
            snapshotService.update(event).ifPresent(this::publishSnapshot);
        }

        kafkaConsumer.commitSync();
    }

    private void publishSnapshot(SensorsSnapshotAvro snapshot) {
        try {
            kafkaProducer.send(
                    new ProducerRecord<>(
                            kafkaProperties.getTopics().getSnapshots(),
                            snapshot.getHubId(),
                            snapshot
                    )
            ).get();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new KafkaProcessingException(
                    "Interrupted while publishing snapshot to Kafka topic " + kafkaProperties.getTopics().getSnapshots(),
                    exception
            );
        } catch (ExecutionException exception) {
            throw new KafkaProcessingException(
                    "Failed to publish snapshot to Kafka topic " + kafkaProperties.getTopics().getSnapshots(),
                    exception
            );
        }
    }
}
