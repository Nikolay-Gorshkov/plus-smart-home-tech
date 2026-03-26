package ru.yandex.practicum.telemetry.analyzer.repository;

import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.telemetry.analyzer.entity.SensorEntity;

public interface SensorRepository extends JpaRepository<SensorEntity, String> {

    boolean existsByIdInAndHubId(Collection<String> ids, String hubId);

    Optional<SensorEntity> findByIdAndHubId(String id, String hubId);
}
