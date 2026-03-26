package ru.yandex.practicum.telemetry.collector.service;

import java.util.concurrent.ExecutionException;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.telemetry.collector.config.KafkaTopicsProperties;
import ru.yandex.practicum.telemetry.collector.exception.KafkaPublishException;
import ru.yandex.practicum.telemetry.collector.mapper.HubEventAvroMapper;
import ru.yandex.practicum.telemetry.collector.mapper.SensorEventAvroMapper;
import ru.yandex.practicum.telemetry.collector.model.hub.HubEvent;
import ru.yandex.practicum.telemetry.collector.model.sensor.SensorEvent;

@Service
public class CollectorEventService {

    private final Producer<String, SpecificRecordBase> kafkaProducer;
    private final KafkaTopicsProperties topicsProperties;
    private final SensorEventAvroMapper sensorEventAvroMapper;
    private final HubEventAvroMapper hubEventAvroMapper;

    public CollectorEventService(
            Producer<String, SpecificRecordBase> kafkaProducer,
            KafkaTopicsProperties topicsProperties,
            SensorEventAvroMapper sensorEventAvroMapper,
            HubEventAvroMapper hubEventAvroMapper
    ) {
        this.kafkaProducer = kafkaProducer;
        this.topicsProperties = topicsProperties;
        this.sensorEventAvroMapper = sensorEventAvroMapper;
        this.hubEventAvroMapper = hubEventAvroMapper;
    }

    public void collectSensorEvent(SensorEvent event) {
        publish(
                topicsProperties.getSensorEvents(),
                event.getHubId(),
                sensorEventAvroMapper.map(event)
        );
    }

    public void collectHubEvent(HubEvent event) {
        publish(
                topicsProperties.getHubEvents(),
                event.getHubId(),
                hubEventAvroMapper.map(event)
        );
    }

    private void publish(String topic, String key, SpecificRecordBase payload) {
        try {
            kafkaProducer.send(new ProducerRecord<>(topic, key, payload)).get();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new KafkaPublishException("Interrupted while publishing event to Kafka topic " + topic, exception);
        } catch (ExecutionException exception) {
            throw new KafkaPublishException("Failed to publish event to Kafka topic " + topic, exception);
        }
    }
}
