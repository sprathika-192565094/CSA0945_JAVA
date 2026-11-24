public class Task10Demo {
    static final class ParkingSlot {
        private final int floor;
        private final int number;
        private String registration;

        ParkingSlot(int floor, int number) {
            if (floor < 0 || number < 0) {
                throw new IllegalArgumentException("Slot coordinates must be non-negative");
            }
            this.floor = floor;
            this.number = number;
        }

        public void occupy(String registration) {
            if (this.registration != null) {
                throw new IllegalStateException("Slot is occupied");
            }
            if (registration == null || registration.isBlank()) {
                throw new IllegalArgumentException("Registration cannot be blank");
            }
            this.registration = registration.trim().toUpperCase();
        }

        public String getRegistration() {
            return registration;
        }

        @Override
        public String toString() {
            return "floor=" + floor + ", slot=" + number + ", vehicle=" + registration;
        }
    }

    public static void main(String[] args) {
        ParkingSlot slot = new ParkingSlot(0, 1);
        slot.occupy("tn01aa1234");
        System.out.println(slot);
    }
}
