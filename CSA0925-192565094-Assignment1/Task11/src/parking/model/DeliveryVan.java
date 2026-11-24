package parking.model;

import parking.pricing.DeliveryVanFeePolicy;
import parking.pricing.ParkingFeePolicy;

public class DeliveryVan extends Car {
    private static final ParkingFeePolicy DELIVERY_POLICY = new DeliveryVanFeePolicy();

    public DeliveryVan(String regNo) {
        super(regNo);
    }

    @Override
    protected ParkingFeePolicy pricingPolicy() {
        return DELIVERY_POLICY;
    }
}
