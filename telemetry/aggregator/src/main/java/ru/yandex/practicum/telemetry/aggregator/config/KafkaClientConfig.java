package ru.yandex.practicum.telemetry.aggregator.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.kafka.serializer.GeneralAvroSerializer;

@Configuration
@EnableConfigurationProperties(AggregatorKafkaProperties.class)
public class KafkaClientConfig {

    @Bean(destroyMethod = "")
    public Consumer<String, byte[]> kafkaConsumer(AggregatorKafkaProperties kafkaProperties) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                String.join(",", kafkaProperties.getBootstrapServers())
        );
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaProperties.getConsumer().getGroupId());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaProperties.getConsumer().getAutoOffsetReset());
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, kafkaProperties.getConsumer().isEnableAutoCommit());
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);

        if (kafkaProperties.getConsumer().getMaxPollRecords() != null) {
            properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, kafkaProperties.getConsumer().getMaxPollRecords());
        }

        if (kafkaProperties.getConsumer().getMaxPartitionFetchBytes() != null) {
            properties.put(
                    ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG,
                    kafkaProperties.getConsumer().getMaxPartitionFetchBytes()
            );
        }

        properties.putAll(kafkaProperties.getConsumer().getProperties());
        return new KafkaConsumer<>(properties);
    }

    @Bean(destroyMethod = "close")
    public Producer<String, SpecificRecordBase> kafkaProducer(AggregatorKafkaProperties kafkaProperties) {
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
        properties.putAll(kafkaProperties.getProducer().getProperties());
        return new KafkaProducer<>(properties);
    }
}
