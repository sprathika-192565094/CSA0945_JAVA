package parking.model;

import parking.pricing.Chargeable;
import parking.pricing.FlatFeePolicy;
import parking.pricing.ParkingFeePolicy;

public class EBike extends TwoWheeler implements Chargeable {
    private static final double FLAT_FEE = 25.0;
    private static final ParkingFeePolicy FLAT_FEE_POLICY = new FlatFeePolicy(FLAT_FEE);

    public EBike(String regNo) {
        super(regNo);
    }

    @Override
    public double calculateFee(int minutes) {
        if (minutes < 0) {
            throw new IllegalArgumentException("Duration cannot be negative");
        }
        return minutes == 0 ? 0.0 : FLAT_FEE;
    }

    @Override
    protected ParkingFeePolicy pricingPolicy() {
        return FLAT_FEE_POLICY;
    }

}
