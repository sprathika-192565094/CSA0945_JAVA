package parking.model;

import java.util.Locale;
import java.util.Objects;

public final class VehicleFactory {
    private VehicleFactory() {
    }

    public static Vehicle create(String type, String registrationNumber) {
        String normalizedType = Objects.requireNonNull(type, "Vehicle type cannot be null")
                .trim().toUpperCase(Locale.ROOT);
        return switch (normalizedType) {
            case "TWO_WHEELER" -> new TwoWheeler(registrationNumber);
            case "CAR" -> new Car(registrationNumber);
            case "ELECTRIC_CAR" -> new ElectricCar(registrationNumber);
            case "E_BIKE" -> new EBike(registrationNumber);
            case "DELIVERY_VAN" -> new DeliveryVan(registrationNumber);
            default -> throw new IllegalArgumentException("Unknown vehicle type: " + type);
        };
    }
}
