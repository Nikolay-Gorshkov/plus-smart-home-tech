package ru.yandex.practicum.commerce.delivery.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import ru.yandex.practicum.commerce.interaction.api.dto.DeliveryState;

@Entity
@Table(name = "deliveries")
public class DeliveryEntity {

    @Id
    @Column(name = "delivery_id", nullable = false)
    private UUID deliveryId;

    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "country", column = @Column(name = "from_country", nullable = false)),
            @AttributeOverride(name = "city", column = @Column(name = "from_city", nullable = false)),
            @AttributeOverride(name = "street", column = @Column(name = "from_street", nullable = false)),
            @AttributeOverride(name = "house", column = @Column(name = "from_house", nullable = false)),
            @AttributeOverride(name = "flat", column = @Column(name = "from_flat"))
    })
    private DeliveryAddressEmbeddable fromAddress;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "country", column = @Column(name = "to_country", nullable = false)),
            @AttributeOverride(name = "city", column = @Column(name = "to_city", nullable = false)),
            @AttributeOverride(name = "street", column = @Column(name = "to_street", nullable = false)),
            @AttributeOverride(name = "house", column = @Column(name = "to_house", nullable = false)),
            @AttributeOverride(name = "flat", column = @Column(name = "to_flat"))
    })
    private DeliveryAddressEmbeddable toAddress;

    @Column(name = "delivery_weight", nullable = false)
    private Double deliveryWeight;

    @Column(name = "delivery_volume", nullable = false)
    private Double deliveryVolume;

    @Column(name = "fragile", nullable = false)
    private Boolean fragile;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private DeliveryState state;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public UUID getDeliveryId() {
        return deliveryId;
    }

    public void setDeliveryId(UUID deliveryId) {
        this.deliveryId = deliveryId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public DeliveryAddressEmbeddable getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(DeliveryAddressEmbeddable fromAddress) {
        this.fromAddress = fromAddress;
    }

    public DeliveryAddressEmbeddable getToAddress() {
        return toAddress;
    }

    public void setToAddress(DeliveryAddressEmbeddable toAddress) {
        this.toAddress = toAddress;
    }

    public Double getDeliveryWeight() {
        return deliveryWeight;
    }

    public void setDeliveryWeight(Double deliveryWeight) {
        this.deliveryWeight = deliveryWeight;
    }

    public Double getDeliveryVolume() {
        return deliveryVolume;
    }

    public void setDeliveryVolume(Double deliveryVolume) {
        this.deliveryVolume = deliveryVolume;
    }

    public Boolean getFragile() {
        return fragile;
    }

    public void setFragile(Boolean fragile) {
        this.fragile = fragile;
    }

    public DeliveryState getState() {
        return state;
    }

    public void setState(DeliveryState state) {
        this.state = state;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
