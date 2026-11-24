package parking.model;

import java.time.LocalDateTime;

public record ParkingReceipt(ParkingTicket ticket, LocalDateTime exitTime, double fee) {
}
