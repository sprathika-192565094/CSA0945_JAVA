public class Task09Demo {
    interface Chargeable {
        default double chargingFee(int kWh) {
            if (kWh < 0) {
                throw new IllegalArgumentException("Energy cannot be negative");
            }
            return kWh * 12.0;
        }
    }

    abstract static class Vehicle {
        private final String registration;

        Vehicle(String registration) {
            this.registration = registration;
        }

        abstract double parkingFee(int minutes);

        String getRegistration() {
            return registration;
        }
    }

    static final class ElectricCar extends Vehicle implements Chargeable {
        ElectricCar(String registration) {
            super(registration);
        }

        @Override
        double parkingFee(int minutes) {
            return ((minutes + 59L) / 60L) * 30.0;
        }
    }

    public static void main(String[] args) {
        Vehicle vehicle = new ElectricCar("TN01AA1234");
        Chargeable chargingCapability = (Chargeable) vehicle;
        System.out.println(vehicle.getRegistration() + " total: Rs "
                + (vehicle.parkingFee(61) + chargingCapability.chargingFee(2)));
    }
}
