package parking.pricing;

import parking.model.Vehicle;

import java.time.Duration;
import java.time.LocalTime;

public interface ParkingFeePolicy {
    double calculateFee(Vehicle vehicle, LocalTime entry, LocalTime exit, Duration duration);
}
