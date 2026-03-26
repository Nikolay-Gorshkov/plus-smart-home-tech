package ru.yandex.practicum.telemetry.collector.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.kafka.serializer.GeneralAvroSerializer;

@Configuration
@EnableConfigurationProperties({KafkaTopicsProperties.class, KafkaProducerProperties.class})
public class KafkaProducerConfig {

    @Bean(destroyMethod = "close")
    public Producer<String, SpecificRecordBase> kafkaProducer(KafkaProducerProperties kafkaProperties) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                String.join(",", kafkaProperties.getBootstrapServers())
        );
        properties.put(ProducerConfig.ACKS_CONFIG, kafkaProperties.getProducer().getAcks());
        properties.put(
                ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,
                kafkaProperties.getProducer().isEnableIdempotence()
        );
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GeneralAvroSerializer.class);
        return new KafkaProducer<>(properties);
    }
}
