package parking.pricing;

import parking.model.Vehicle;

import java.time.Duration;
import java.time.LocalTime;

public class FlatFeePolicy implements ParkingFeePolicy {
    private final double flatFee;

    public FlatFeePolicy(double flatFee) {
        if (flatFee < 0.0) {
            throw new IllegalArgumentException("Flat fee cannot be negative");
        }
        this.flatFee = flatFee;
    }

    @Override
    public double calculateFee(Vehicle vehicle, LocalTime entry, LocalTime exit, Duration duration) {
        return duration.isZero() ? 0.0 : flatFee;
    }
}
