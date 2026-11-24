package parking.model;

public final class ParkingSlot {
    private final int floor;
    private final int number;
    private final boolean charging;
    private ParkingTicket activeTicket;

    public ParkingSlot(int floor, int number, boolean charging) {
        if (floor < 0 || number < 0) {
            throw new IllegalArgumentException("Floor and slot number must be non-negative");
        }
        this.floor = floor;
        this.number = number;
        this.charging = charging;
    }

    public int getFloor() {
        return floor;
    }

    public int getNumber() {
        return number;
    }

    public boolean isCharging() {
        return charging;
    }

    public boolean isFree() {
        return activeTicket == null;
    }

    public ParkingTicket getActiveTicket() {
        return activeTicket;
    }

    public void occupy(ParkingTicket ticket) {
        if (!isFree()) {
            throw new IllegalStateException("Slot is already occupied");
        }
        activeTicket = ticket;
    }

    public void release() {
        if (isFree()) {
            throw new IllegalStateException("Slot is already free");
        }
        activeTicket = null;
    }
}
