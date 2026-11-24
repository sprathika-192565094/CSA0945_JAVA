import java.util.Objects;

public class Task04Demo {
    static final class Vehicle {
        private final String registration;
        private final String category;

        Vehicle(String registration) {
            this(registration, "CAR");
        }

        Vehicle(String registration, String category) {
            this.registration = Objects.requireNonNull(registration).trim().toUpperCase();
            this.category = Objects.requireNonNull(category).trim().toUpperCase();
            if (this.registration.isEmpty() || this.category.isEmpty()) {
                throw new IllegalArgumentException("Registration and category are required");
            }
        }

        Vehicle(Vehicle other) {
            this(Objects.requireNonNull(other).registration, other.category);
        }

        @Override
        public String toString() {
            return category + " " + registration;
        }
    }

    public static void main(String[] args) {
        Vehicle original = new Vehicle("tn01aa1234");
        Vehicle copied = new Vehicle(original);
        System.out.println(original);
        System.out.println(copied);
    }
}
