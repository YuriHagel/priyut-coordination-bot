package ru.priyut.coordination.domain.volunteer;

import ru.priyut.coordination.domain.shared.DomainException;

import java.util.Objects;

/**
 * Информация об автомобиле волонтёра.
 * Record: неизменяемый, с валидацией.
 */
public record Car(String brand, String model, String color) {

    public Car {
        Objects.requireNonNull(brand, "Car brand must not be null");
        Objects.requireNonNull(model, "Car model must not be null");

        brand = brand.trim();
        model = model.trim();
        color = color == null ? "" : color.trim();

        if (brand.isEmpty()) {
            throw new DomainException("Car brand must not be empty");
        }
        if (model.isEmpty()) {
            throw new DomainException("Car model must not be empty");
        }
        if (brand.length() > 50 || model.length() > 50 || color.length() > 50) {
            throw new DomainException("Car fields are too long");
        }
    }

    public static Car of(String brand, String model, String color) {
        return new Car(brand, model, color);
    }

    /**
     * Форматированное представление: "Kia Rio, серый"
     */
    public String formatted() {
        if (color.isEmpty()) {
            return brand + " " + model;
        }
        return brand + " " + model + ", " + color;
    }

    @Override
    public String toString() {
        return formatted();
    }
}