package parking.pricing;

import parking.model.Vehicle;

import java.time.Duration;
import java.time.LocalTime;

public class StandardFeePolicy implements ParkingFeePolicy {
    private static final long SECONDS_PER_HOUR = 60L * 60L;
    private static final long SECONDS_PER_DAY = 24L * SECONDS_PER_HOUR;
    private static final int MAX_CHARGED_HOURS_PER_DAY = 10;

    @Override
    public double calculateFee(Vehicle vehicle, LocalTime entry, LocalTime exit, Duration duration) {
        long seconds = duration.getSeconds();
        long hours = (seconds + SECONDS_PER_HOUR - 1) / SECONDS_PER_HOUR;
        long days = Math.max(1, (seconds + SECONDS_PER_DAY - 1) / SECONDS_PER_DAY);
        double hourlyFee = hours * vehicle.ratePerHour();
        double dailyMaximum = days * MAX_CHARGED_HOURS_PER_DAY * vehicle.ratePerHour();
        return Math.min(hourlyFee, dailyMaximum);
    }
}
