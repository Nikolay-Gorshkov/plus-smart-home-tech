package ru.yandex.practicum.telemetry.aggregator.exception;

public class KafkaProcessingException extends RuntimeException {

    public KafkaProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
