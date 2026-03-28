package ru.yandex.practicum.commerce.warehouse.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import ru.yandex.practicum.commerce.interaction.api.dto.AddressDto;

@ConfigurationProperties(prefix = "warehouse.address")
public class WarehouseAddressProperties {

    private String country = "ADDRESS_1";
    private String city = "ADDRESS_1";
    private String street = "ADDRESS_1";
    private String house = "1";
    private String flat = "";

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
