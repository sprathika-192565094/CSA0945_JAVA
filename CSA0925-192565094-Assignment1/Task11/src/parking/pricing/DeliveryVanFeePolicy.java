package parking.pricing;

import parking.model.Vehicle;

import java.time.Duration;
import java.time.LocalTime;

public class DeliveryVanFeePolicy implements ParkingFeePolicy {
    private static final long SECONDS_PER_HOUR = 60L * 60L;
    private static final long SECONDS_PER_DAY = 24L * SECONDS_PER_HOUR;
    private static final int MAX_CHARGED_HOURS_PER_DAY = 10;
    private static final LocalTime PEAK_START = LocalTime.of(9, 0);
    private static final LocalTime PEAK_END = LocalTime.of(17, 0);

    @Override
    public double calculateFee(Vehicle vehicle, LocalTime entry, LocalTime exit, Duration duration) {
        long seconds = duration.getSeconds();
        long hours = (seconds + SECONDS_PER_HOUR - 1) / SECONDS_PER_HOUR;
        long days = Math.max(1, (seconds + SECONDS_PER_DAY - 1) / SECONDS_PER_DAY);
        double total = 0.0;
        for (long hour = 0; hour < hours; hour++) {
            LocalTime billedHourStart = entry.plusSeconds(hour * SECONDS_PER_HOUR);
            boolean peak = !billedHourStart.isBefore(PEAK_START) && billedHourStart.isBefore(PEAK_END);
            total += vehicle.ratePerHour() * (peak ? 2.0 : 1.0);
        }
        double dailyMaximum = days * MAX_CHARGED_HOURS_PER_DAY * vehicle.ratePerHour() * 2.0;
        return Math.min(total, dailyMaximum);
    }
}
