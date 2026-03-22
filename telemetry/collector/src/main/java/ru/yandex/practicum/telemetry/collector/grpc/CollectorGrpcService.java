package ru.yandex.practicum.telemetry.collector.grpc;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.telemetry.collector.mapper.HubEventProtoMapper;
import ru.yandex.practicum.telemetry.collector.mapper.SensorEventProtoMapper;
import ru.yandex.practicum.telemetry.collector.service.CollectorEventService;

@GrpcService
public class CollectorGrpcService extends CollectorControllerGrpc.CollectorControllerImplBase {

    private final CollectorEventService collectorEventService;
    private final SensorEventProtoMapper sensorEventProtoMapper;
    private final HubEventProtoMapper hubEventProtoMapper;
    private final Validator validator;

    public CollectorGrpcService(
            CollectorEventService collectorEventService,
            SensorEventProtoMapper sensorEventProtoMapper,
            HubEventProtoMapper hubEventProtoMapper,
            Validator validator
    ) {
        this.collectorEventService = collectorEventService;
        this.sensorEventProtoMapper = sensorEventProtoMapper;
        this.hubEventProtoMapper = hubEventProtoMapper;
        this.validator = validator;
    }

    @Override
    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
        handle(responseObserver, () -> collectorEventService.collectSensorEvent(validate(sensorEventProtoMapper.map(request))));
    }

    @Override
    public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
        handle(responseObserver, () -> collectorEventService.collectHubEvent(validate(hubEventProtoMapper.map(request))));
    }

    private void handle(StreamObserver<Empty> responseObserver, Runnable action) {
        try {
            action.run();
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException exception) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription(exception.getMessage())
                            .withCause(exception)
                            .asRuntimeException()
            );
        } catch (RuntimeException exception) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription(exception.getMessage())
                            .withCause(exception)
                            .asRuntimeException()
            );
        }
    }

    private <T> T validate(T event) {
        Set<ConstraintViolation<T>> violations = validator.validate(event);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException(violations.stream()
                    .map(this::formatViolation)
                    .collect(Collectors.joining("; ")));
        }
        return event;
    }

    private String formatViolation(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath().toString();
        return path.isBlank() ? violation.getMessage() : path + " " + violation.getMessage();
    }
}
