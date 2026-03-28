package ru.yandex.practicum.commerce.delivery.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import ru.yandex.practicum.commerce.interaction.api.dto.AddressDto;

@Embeddable
public class DeliveryAddressEmbeddable {

    @Column(name = "country", nullable = false)
    private String country;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "street", nullable = false)
    private String street;

    @Column(name = "house", nullable = false)
    private String house;

    @Column(name = "flat")
    private String flat;

    public static DeliveryAddressEmbeddable fromDto(AddressDto addressDto) {
        DeliveryAddressEmbeddable embeddable = new DeliveryAddressEmbeddable();
        embeddable.setCountry(addressDto.country());
        embeddable.setCity(addressDto.city());
        embeddable.setStreet(addressDto.street());
        embeddable.setHouse(addressDto.house());
        embeddable.setFlat(addressDto.flat());
        return embeddable;
    }

    public AddressDto toDto() {
        return new AddressDto(country, city, street, house, flat);
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getHouse() {
        return house;
    }

    public void setHouse(String house) {
        this.house = house;
    }

    public String getFlat() {
        return flat;
    }

    public void setFlat(String flat) {
        this.flat = flat;
    }
}
