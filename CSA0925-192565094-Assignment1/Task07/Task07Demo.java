import java.util.Objects;

public class Task07Demo {
    abstract static class Vehicle {
        private final String registration;

        Vehicle(String registration) {
            this.registration = Objects.requireNonNull(registration).toUpperCase();
        }

        abstract double hourlyRate();

        double fee(int minutes) {
            if (minutes < 0) {
                throw new IllegalArgumentException("Duration cannot be negative");
            }
            long hours = (minutes + 59L) / 60L;
            return hours * hourlyRate();
        }

        @Override
        public final boolean equals(Object other) {
            return other instanceof Vehicle vehicle
                    && registration.equals(vehicle.registration);
        }

        @Override
        public final int hashCode() {
            return registration.hashCode();
        }
    }

    static final class Car extends Vehicle {
        Car(String registration) {
            super(registration);
        }

        @Override
        double hourlyRate() {
            return 30.0;
        }
    }

    public static void main(String[] args) {
        Vehicle car = new Car("TN01AA1234");
        System.out.println("61-minute fee: Rs " + car.fee(61));
    }
}
