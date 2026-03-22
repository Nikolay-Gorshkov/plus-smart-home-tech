package ru.yandex.practicum.telemetry.analyzer.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "scenario_actions")
public class ScenarioActionLinkEntity {

    @EmbeddedId
    private ScenarioActionLinkId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scenario_id", insertable = false, updatable = false)
    private ScenarioEntity scenario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sensor_id", insertable = false, updatable = false)
    private SensorEntity sensor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "action_id", insertable = false, updatable = false)
    private ActionEntity action;

    public ScenarioActionLinkEntity() {
    }

    public ScenarioActionLinkEntity(ScenarioEntity scenario, SensorEntity sensor, ActionEntity action) {
        this.id = new ScenarioActionLinkId(scenario.getId(), sensor.getId(), action.getId());
        this.scenario = scenario;
        this.sensor = sensor;
        this.action = action;
    }

    public ScenarioActionLinkId getId() {
        return id;
    }

    public ScenarioEntity getScenario() {
        return scenario;
    }

    public SensorEntity getSensor() {
        return sensor;
    }

    public ActionEntity getAction() {
        return action;
    }
}
