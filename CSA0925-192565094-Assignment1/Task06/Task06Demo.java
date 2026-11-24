public class Task06Demo {
    abstract static class Vehicle {
        private final String registration;

        Vehicle(String registration) {
            this.registration = registration;
            System.out.println("Vehicle constructor");
        }

        String getRegistration() {
            return registration;
        }
    }

    static class Car extends Vehicle {
        Car(String registration) {
            super(registration);
            System.out.println("Car constructor");
        }
    }

    static final class ElectricCar extends Car {
        ElectricCar(String registration) {
            super(registration);
            System.out.println("ElectricCar constructor");
        }
    }

    public static void main(String[] args) {
        Vehicle vehicle = new ElectricCar("TN01AA1234");
        System.out.println(vehicle.getRegistration());
    }
}
