package ru.yandex.practicum.commerce.delivery.config;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "delivery")
public class DeliveryPricingProperties {

    private BigDecimal baseCost = BigDecimal.valueOf(5.0);
    private BigDecimal fragileFactor = BigDecimal.valueOf(0.2);
    private BigDecimal weightFactor = BigDecimal.valueOf(0.3);
    private BigDecimal volumeFactor = BigDecimal.valueOf(0.2);
    private BigDecimal streetFactor = BigDecimal.valueOf(0.2);

    public BigDecimal getBaseCost() {
        return baseCost;
    }

    public void setBaseCost(BigDecimal baseCost) {
        this.baseCost = baseCost;
    }

    public BigDecimal getFragileFactor() {
        return fragileFactor;
    }

    public void setFragileFactor(BigDecimal fragileFactor) {
        this.fragileFactor = fragileFactor;
    }

    public BigDecimal getWeightFactor() {
        return weightFactor;
    }

    public void setWeightFactor(BigDecimal weightFactor) {
        this.weightFactor = weightFactor;
    }

    public BigDecimal getVolumeFactor() {
        return volumeFactor;
    }

    public void setVolumeFactor(BigDecimal volumeFactor) {
        this.volumeFactor = volumeFactor;
    }

    public BigDecimal getStreetFactor() {
        return streetFactor;
    }

    public void setStreetFactor(BigDecimal streetFactor) {
        this.streetFactor = streetFactor;
    }
}
