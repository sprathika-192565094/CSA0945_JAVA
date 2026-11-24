package parking.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public final class ParkingTicket {
    private static final AtomicLong NEXT_NUMBER = new AtomicLong(1);

    private final String ticketNumber;
    private final Vehicle vehicle;
    private final ParkingSlot slot;
    private final LocalDateTime entryTime;
    private final ParkingPass pass;

    public ParkingTicket(Vehicle vehicle, ParkingSlot slot, LocalDateTime entryTime, ParkingPass pass) {
        this.ticketNumber = "PARK-" + String.format("%06d", NEXT_NUMBER.getAndIncrement());
        this.vehicle = Objects.requireNonNull(vehicle, "Vehicle cannot be null");
        this.slot = Objects.requireNonNull(slot, "Slot cannot be null");
        this.entryTime = Objects.requireNonNull(entryTime, "Entry time cannot be null");
        this.pass = pass;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public ParkingSlot getSlot() {
        return slot;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public boolean hasValidPassAt(LocalDateTime time) {
        return pass != null && pass.isValidAt(time);
    }
}
