import java.util.Objects;

public class Task01Demo {
    static final class Vehicle {
        private final String registration;

        Vehicle(String registration) {
            this.registration = Objects.requireNonNull(registration).trim().toUpperCase();
            if (this.registration.isEmpty()) {
                throw new IllegalArgumentException("Registration cannot be blank");
            }
        }

        String getRegistration() {
            return registration;
        }
    }

    static final class ParkingTicket {
        private final Vehicle vehicle;
        private final int floor;
        private final int slot;

        ParkingTicket(Vehicle vehicle, int floor, int slot) {
            this.vehicle = Objects.requireNonNull(vehicle);
            this.floor = floor;
            this.slot = slot;
        }

        @Override
        public String toString() {
            return vehicle.getRegistration() + " at floor " + floor + ", slot " + slot;
        }
    }

    public static void main(String[] args) {
        ParkingTicket ticket = new ParkingTicket(new Vehicle("tn01aa1234"), 0, 3);
        System.out.println(ticket);
    }
}
