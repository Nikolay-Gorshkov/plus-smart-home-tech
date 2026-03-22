package ru.yandex.practicum.telemetry.analyzer.service;

import com.google.protobuf.Timestamp;
import java.time.Instant;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc.HubRouterControllerBlockingStub;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

@Service
public class HubRouterActionService {

    private final HubRouterControllerBlockingStub hubRouterClient;

    public HubRouterActionService(@GrpcClient("hub-router") HubRouterControllerBlockingStub hubRouterClient) {
        this.hubRouterClient = hubRouterClient;
    }

    public void execute(SensorsSnapshotAvro snapshot, ScenarioDefinition scenario) {
        for (ScenarioDefinition.ActionDefinition action : scenario.actions()) {
            DeviceActionProto.Builder actionBuilder = DeviceActionProto.newBuilder()
                    .setSensorId(action.sensorId())
                    .setType(ActionTypeProto.valueOf(action.type().name()));

            if (action.value() != null) {
                actionBuilder.setValue(action.value());
            }

            DeviceActionRequest request = DeviceActionRequest.newBuilder()
                    .setHubId(snapshot.getHubId())
                    .setScenarioName(scenario.name())
                    .setAction(actionBuilder.build())
                    .setTimestamp(toTimestamp(snapshot.getTimestamp()))
                    .build();

            hubRouterClient.handleDeviceAction(request);
        }
    }

    private Timestamp toTimestamp(Instant instant) {
        Instant effectiveInstant = instant == null ? Instant.now() : instant;
        return Timestamp.newBuilder()
                .setSeconds(effectiveInstant.getEpochSecond())
                .setNanos(effectiveInstant.getNano())
                .build();
    }
}
