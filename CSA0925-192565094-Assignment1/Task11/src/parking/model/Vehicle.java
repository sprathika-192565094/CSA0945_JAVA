package parking.model;

import parking.pricing.ParkingFeePolicy;
import parking.pricing.StandardFeePolicy;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

public abstract class Vehicle {
    private final String regNo;
    private static final ParkingFeePolicy STANDARD_POLICY = new StandardFeePolicy();

    protected Vehicle(String regNo) {
        String normalized = Objects.requireNonNull(regNo, "Registration number cannot be null")
                .trim().toUpperCase();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Registration number cannot be blank");
        }
        this.regNo = normalized;
    }

    public final String getRegNo() {
        return regNo;
    }

    public abstract double ratePerHour();

    protected ParkingFeePolicy pricingPolicy() {
        return STANDARD_POLICY;
    }

    public double calculateFee(int minutes) {
        if (minutes < 0) {
            throw new IllegalArgumentException("Duration cannot be negative");
        }
        long billableHours = (minutes + 59L) / 60L;
        return billableHours * ratePerHour();
    }

    public double calculateFee(LocalTime entry, LocalTime exit) {
        Objects.requireNonNull(entry, "Entry time cannot be null");
        Objects.requireNonNull(exit, "Exit time cannot be null");
        long seconds = Duration.between(entry, exit).getSeconds();
        if (seconds < 0) {
            seconds += Duration.ofDays(1).getSeconds();
        }
        return pricingPolicy().calculateFee(this, entry, exit, Duration.ofSeconds(seconds));
    }

    public double calculateFee(LocalDateTime entry, LocalDateTime exit) {
        Objects.requireNonNull(entry, "Entry time cannot be null");
        Objects.requireNonNull(exit, "Exit time cannot be null");
        Duration duration = Duration.between(entry, exit);
        if (duration.isNegative()) {
            throw new IllegalArgumentException("Exit time cannot be before entry time");
        }
        return pricingPolicy().calculateFee(this, entry.toLocalTime(), exit.toLocalTime(), duration);
    }

    @Override
    public final String toString() {
        return getClass().getSimpleName() + "{regNo='" + regNo + "'}";
    }

    @Override
    public final boolean equals(Object other) {
        return other instanceof Vehicle vehicle && regNo.equals(vehicle.regNo);
    }

    @Override
    public final int hashCode() {
        return regNo.hashCode();
    }
}
