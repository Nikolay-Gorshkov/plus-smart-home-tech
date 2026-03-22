package ru.yandex.practicum.kafka.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

public class GeneralAvroSerializer implements Serializer<SpecificRecordBase> {

    private final EncoderFactory encoderFactory;
    private BinaryEncoder encoder;

    public GeneralAvroSerializer() {
        this(EncoderFactory.get());
    }

    public GeneralAvroSerializer(EncoderFactory encoderFactory) {
        this.encoderFactory = encoderFactory;
    }

    @Override
    public byte[] serialize(String topic, SpecificRecordBase data) {
        if (data == null) {
            return null;
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            encoder = encoderFactory.binaryEncoder(outputStream, encoder);
            new SpecificDatumWriter<>(data.getSchema()).write(data, encoder);
            encoder.flush();
            outputStream.flush();
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new SerializationException("Failed to serialize Avro message for topic " + topic, exception);
        }
    }
}
