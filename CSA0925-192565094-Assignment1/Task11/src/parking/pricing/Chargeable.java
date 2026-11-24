package parking.pricing;

public interface Chargeable {
    default double chargingFee(int kWh) {
        if (kWh < 0) {
            throw new IllegalArgumentException("Energy usage cannot be negative");
        }
        return kWh * 12.0;
    }
}
