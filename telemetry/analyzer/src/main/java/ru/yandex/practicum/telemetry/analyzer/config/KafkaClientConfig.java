package ru.yandex.practicum.telemetry.analyzer.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AnalyzerKafkaProperties.class)
public class KafkaClientConfig {

    @Bean(destroyMethod = "")
    public Consumer<String, byte[]> hubEventKafkaConsumer(AnalyzerKafkaProperties kafkaProperties) {
        return createConsumer(kafkaProperties.getBootstrapServers(), kafkaProperties.getHubConsumer());
    }

    @Bean(destroyMethod = "")
    public Consumer<String, byte[]> snapshotKafkaConsumer(AnalyzerKafkaProperties kafkaProperties) {
        return createConsumer(kafkaProperties.getBootstrapServers(), kafkaProperties.getSnapshotConsumer());
    }

    private Consumer<String, byte[]> createConsumer(
            Iterable<String> bootstrapServers,
            AnalyzerKafkaProperties.Consumer consumerProperties
    ) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, String.join(",", bootstrapServers));
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, consumerProperties.getGroupId());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, consumerProperties.getAutoOffsetReset());
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, consumerProperties.isEnableAutoCommit());
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);

        if (consumerProperties.getMaxPollRecords() != null) {
            properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, consumerProperties.getMaxPollRecords());
        }

        if (consumerProperties.getMaxPartitionFetchBytes() != null) {
            properties.put(
                    ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG,
                    consumerProperties.getMaxPartitionFetchBytes()
            );
        }

        properties.putAll(consumerProperties.getProperties());
        return new KafkaConsumer<>(properties);
    }
}
