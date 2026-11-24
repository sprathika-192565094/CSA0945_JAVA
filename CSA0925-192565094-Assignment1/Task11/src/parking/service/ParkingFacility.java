package parking.service;

import parking.model.ParkingPass;
import parking.model.ParkingReceipt;
import parking.model.ParkingSlot;
import parking.model.ParkingTicket;
import parking.model.Vehicle;
import parking.pricing.Chargeable;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class ParkingFacility {
    private final ParkingSlot[][] floors;
    private final Map<String, ParkingTicket> activeTickets = new HashMap<>();

    public ParkingFacility(int[] slotsPerFloor) {
        this(slotsPerFloor, new int[slotsPerFloor.length][]);
    }

    public ParkingFacility(int[] slotsPerFloor, int[][] chargingSlotsByFloor) {
        Objects.requireNonNull(slotsPerFloor, "Floor configuration cannot be null");
        Objects.requireNonNull(chargingSlotsByFloor, "Charging-slot configuration cannot be null");
        if (slotsPerFloor.length == 0 || chargingSlotsByFloor.length != slotsPerFloor.length) {
            throw new IllegalArgumentException("Each facility must have floors and matching slot configurations");
        }
        floors = new ParkingSlot[slotsPerFloor.length][];
        for (int floor = 0; floor < slotsPerFloor.length; floor++) {
            if (slotsPerFloor[floor] < 0) {
                throw new IllegalArgumentException("Slot count cannot be negative");
            }
            int[] chargingSlots = chargingSlotsByFloor[floor] == null
                    ? new int[0] : chargingSlotsByFloor[floor];
            floors[floor] = new ParkingSlot[slotsPerFloor[floor]];
            for (int slot = 0; slot < slotsPerFloor[floor]; slot++) {
                floors[floor][slot] = new ParkingSlot(
                        floor, slot, contains(chargingSlots, slot));
            }
            for (int chargingSlot : chargingSlots) {
                if (chargingSlot < 0 || chargingSlot >= slotsPerFloor[floor]) {
                    throw new IllegalArgumentException("Charging slot index is outside its floor");
                }
            }
        }
    }

    public synchronized ParkingTicket enter(Vehicle vehicle, LocalDateTime entryTime, ParkingPass pass) {
        Objects.requireNonNull(vehicle, "Vehicle cannot be null");
        Objects.requireNonNull(entryTime, "Entry time cannot be null");
        if (activeTickets.containsKey(vehicle.getRegNo())) {
            throw new IllegalArgumentException("This registration number already has an active ticket");
        }

        ParkingSlot slot = findFreeSlot(vehicle);
        if (slot == null) {
            throw new IllegalStateException("No suitable parking slot is available");
        }

        ParkingTicket ticket = new ParkingTicket(vehicle, slot, entryTime, pass);
        slot.occupy(ticket);
        activeTickets.put(vehicle.getRegNo(), ticket);
        return ticket;
    }

    public synchronized ParkingReceipt exit(String registrationNumber, LocalDateTime exitTime) {
        return exit(registrationNumber, exitTime, 0);
    }

    public synchronized ParkingReceipt exit(String registrationNumber, LocalDateTime exitTime, int kWh) {
        Objects.requireNonNull(registrationNumber, "Registration number cannot be null");
        Objects.requireNonNull(exitTime, "Exit time cannot be null");
        if (kWh < 0) {
            throw new IllegalArgumentException("Energy usage cannot be negative");
        }
        ParkingTicket ticket = activeTickets.get(registrationNumber.trim().toUpperCase());
        if (ticket == null) {
            throw new IllegalArgumentException("No active ticket for registration number");
        }
        if (exitTime.isBefore(ticket.getEntryTime())) {
            throw new IllegalArgumentException("Exit time cannot be before entry time");
        }
        if (kWh > 0 && !(ticket.getVehicle() instanceof Chargeable)) {
            throw new IllegalArgumentException("Charging energy can only be billed to a chargeable vehicle");
        }

        boolean passCoversStay = ticket.hasValidPassAt(ticket.getEntryTime())
                && ticket.hasValidPassAt(exitTime);
        double fee = passCoversStay
                ? 0.0 : ticket.getVehicle().calculateFee(ticket.getEntryTime(), exitTime);
        if (ticket.getVehicle() instanceof Chargeable chargeable) {
            fee += chargeable.chargingFee(kWh);
        }
        ParkingReceipt receipt = new ParkingReceipt(ticket, exitTime, fee);
        ticket.getSlot().release();
        activeTickets.remove(ticket.getVehicle().getRegNo());
        return receipt;
    }

    public synchronized int getActiveVehicleCount() {
        return activeTickets.size();
    }

    private ParkingSlot findFreeSlot(Vehicle vehicle) {
        boolean chargeable = vehicle instanceof Chargeable;
        if (chargeable) {
            for (ParkingSlot[] floor : floors) {
                for (ParkingSlot slot : floor) {
                    if (slot.isFree() && slot.isCharging()) {
                        return slot;
                    }
                }
            }
        }
        for (ParkingSlot[] floor : floors) {
            for (ParkingSlot slot : floor) {
                if (slot.isFree() && (!slot.isCharging() || chargeable)) {
                    return slot;
                }
            }
        }
        if (!chargeable) {
            for (ParkingSlot[] floor : floors) {
                for (ParkingSlot slot : floor) {
                    if (slot.isFree()) {
                        return slot;
                    }
                }
            }
        }
        return null;
    }

    private static boolean contains(int[] values, int target) {
        if (values == null) {
            return false;
        }
        for (int value : values) {
            if (value == target) {
                return true;
            }
        }
        return false;
    }
}
