package ru.yandex.practicum.telemetry.analyzer.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import ru.yandex.practicum.telemetry.analyzer.db.HubIdIntegrityValidator;

@Entity
@Table(name = "scenario_conditions")
public class ScenarioConditionLinkEntity {

    @EmbeddedId
    private ScenarioConditionLinkId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scenario_id", insertable = false, updatable = false)
    private ScenarioEntity scenario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sensor_id", insertable = false, updatable = false)
    private SensorEntity sensor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "condition_id", insertable = false, updatable = false)
    private ConditionEntity condition;

    public ScenarioConditionLinkEntity() {
    }

    public ScenarioConditionLinkEntity(ScenarioEntity scenario, SensorEntity sensor, ConditionEntity condition) {
        HubIdIntegrityValidator.validate(scenario, sensor);
        this.id = new ScenarioConditionLinkId(scenario.getId(), sensor.getId(), condition.getId());
        this.scenario = scenario;
        this.sensor = sensor;
        this.condition = condition;
    }

    public ScenarioConditionLinkId getId() {
        return id;
    }

    public ScenarioEntity getScenario() {
        return scenario;
    }

    public SensorEntity getSensor() {
        return sensor;
    }

    public ConditionEntity getCondition() {
        return condition;
    }

    @PrePersist
    @PreUpdate
    private void validateHubIdIntegrity() {
        HubIdIntegrityValidator.validate(scenario, sensor);
    }
}
