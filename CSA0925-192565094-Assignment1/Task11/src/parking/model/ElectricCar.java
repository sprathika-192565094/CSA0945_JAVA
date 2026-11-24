package parking.model;

import parking.pricing.Chargeable;

public class ElectricCar extends Car implements Chargeable {
    public ElectricCar(String regNo) {
        super(regNo);
    }
}
