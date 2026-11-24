package parking.app;

import parking.model.Car;
import parking.model.DeliveryVan;
import parking.model.ElectricCar;
import parking.model.MonthlyPass;
import parking.model.OneDayPass;
import parking.model.ParkingReceipt;
import parking.model.ParkingTicket;
import parking.model.TwoWheeler;
import parking.service.ParkingFacility;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

public final class ParkingDemo {
    private ParkingDemo() {
    }

    public static void main(String[] args) {
        ParkingFacility facility = new ParkingFacility(
                new int[]{3, 2, 2, 2, 2},
                new int[][]{{2}, {}, {0}, {}, {1}});
        LocalDateTime start = LocalDateTime.of(2026, 11, 14, 8, 0);

        ParkingTicket car = facility.enter(new Car("TN01AA1001"), start, null);
        ParkingTicket ev = facility.enter(new ElectricCar("TN02BB2002"), start, null);
        ParkingTicket student = facility.enter(new TwoWheeler("TN03CC3003"), start,
                new MonthlyPass(YearMonth.from(start)));
        ParkingTicket visitor = facility.enter(new TwoWheeler("TN04DD4004"), start,
                new OneDayPass(LocalDate.from(start)));
        ParkingTicket van = facility.enter(new DeliveryVan("TN05EE5005"),
                LocalDateTime.of(2026, 11, 14, 8, 0), null);

        print(facility.exit(car.getVehicle().getRegNo(), start.plusMinutes(60)));
        print(facility.exit(ev.getVehicle().getRegNo(), start.plusMinutes(61), 3));
        print(facility.exit(student.getVehicle().getRegNo(), start.plusHours(2)));
        print(facility.exit(visitor.getVehicle().getRegNo(), start.plusMinutes(30)));
        print(facility.exit(van.getVehicle().getRegNo(),
                LocalDateTime.of(2026, 11, 14, 10, 0)));
        System.out.println("Active vehicles: " + facility.getActiveVehicleCount());
    }

    private static void print(ParkingReceipt receipt) {
        System.out.printf("%s slot=(%d,%d) fee=Rs %.2f%n",
                receipt.ticket().getTicketNumber(),
                receipt.ticket().getSlot().getFloor(),
                receipt.ticket().getSlot().getNumber(),
                receipt.fee());
    }
}
