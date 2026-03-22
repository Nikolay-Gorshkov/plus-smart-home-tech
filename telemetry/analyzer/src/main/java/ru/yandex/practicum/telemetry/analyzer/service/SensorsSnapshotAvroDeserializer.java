package ru.yandex.practicum.telemetry.analyzer.service;

import java.io.IOException;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.common.errors.SerializationException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

@Component
public class SensorsSnapshotAvroDeserializer {

    private final SpecificDatumReader<SensorsSnapshotAvro> reader =
            new SpecificDatumReader<>(SensorsSnapshotAvro.getClassSchema());
    private BinaryDecoder decoder;

    public SensorsSnapshotAvro deserialize(String topic, byte[] data) {
        if (data == null) {
            throw new SerializationException("Received null payload from topic " + topic);
        }

        try {
            decoder = DecoderFactory.get().binaryDecoder(data, decoder);
            return reader.read(null, decoder);
        } catch (IOException exception) {
            throw new SerializationException("Failed to deserialize Avro message from topic " + topic, exception);
        }
    }
}
