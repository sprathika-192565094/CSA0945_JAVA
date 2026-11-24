package parking.app;

import parking.model.Car;
import parking.model.DeliveryVan;
import parking.model.EBike;
import parking.model.ElectricCar;
import parking.model.MonthlyPass;
import parking.model.OneDayPass;
import parking.model.ParkingReceipt;
import parking.model.ParkingTicket;
import parking.model.TwoWheeler;
import parking.model.VehicleFactory;
import parking.service.ParkingFacility;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

public final class ParkingSystemTest {
    private static int passed;

    private ParkingSystemTest() {
    }

    public static void main(String[] args) {
        test("normal car, exactly one hour", () -> {
            ParkingFacility facility = new ParkingFacility(new int[]{1});
            ParkingTicket ticket = facility.enter(new Car("TN01AA0001"), time(8, 0), null);
            check(facility.exit(ticket.getVehicle().getRegNo(), time(9, 0)).fee() == 30.0);
        });
        test("partial hour rounds up", () -> {
            ParkingFacility facility = new ParkingFacility(new int[]{1});
            ParkingTicket ticket = facility.enter(new TwoWheeler("TN01AA0002"), time(8, 0), null);
            check(facility.exit(ticket.getVehicle().getRegNo(), time(9, 1)).fee() == 20.0);
        });
        test("zero-minute stay costs zero", () -> {
            ParkingFacility facility = new ParkingFacility(new int[]{1});
            ParkingTicket ticket = facility.enter(new Car("TN01AA0003"), time(8, 0), null);
            check(facility.exit(ticket.getVehicle().getRegNo(), time(8, 0)).fee() == 0.0);
        });
        test("last free slot and full-facility rejection", () -> {
            ParkingFacility facility = new ParkingFacility(new int[]{1, 1});
            facility.enter(new Car("TN01AA0004"), time(8, 0), null);
            ParkingTicket last = facility.enter(new Car("TN01AA0005"), time(8, 0), null);
            check(last.getSlot().getFloor() == 1 && last.getSlot().getNumber() == 0);
            expect(IllegalStateException.class,
                    () -> facility.enter(new Car("TN01AA0006"), time(8, 0), null));
        });
        test("duplicate active registration rejected", () -> {
            ParkingFacility facility = new ParkingFacility(new int[]{2});
            facility.enter(new Car("TN01AA0007"), time(8, 0), null);
            expect(IllegalArgumentException.class,
                    () -> facility.enter(new Car("tn01aa0007"), time(8, 0), null));
        });
        test("unknown vehicle category rejected", () ->
                expect(IllegalArgumentException.class,
                        () -> VehicleFactory.create("spaceship", "TN01AA0098")));
        test("invalid exit time preserves ticket", () -> {
            ParkingFacility facility = new ParkingFacility(new int[]{1});
            ParkingTicket ticket = facility.enter(new Car("TN01AA0008"), time(8, 0), null);
            expect(IllegalArgumentException.class,
                    () -> facility.exit(ticket.getVehicle().getRegNo(), time(7, 59)));
            check(facility.getActiveVehicleCount() == 1);
        });
        test("out-of-range slot configuration rejected", () ->
                expect(IllegalArgumentException.class,
                        () -> new ParkingFacility(new int[]{1}, new int[][]{{1}})));
        test("monthly and one-day passes waive parking fee", () -> {
            ParkingFacility facility = new ParkingFacility(new int[]{2});
            LocalDateTime entry = time(8, 0);
            ParkingTicket staff = facility.enter(new TwoWheeler("TN01AA0009"), entry,
                    new MonthlyPass(YearMonth.from(entry)));
            ParkingTicket visitor = facility.enter(new Car("TN01AA0010"), entry,
                    new OneDayPass(LocalDate.from(entry)));
            check(facility.exit(staff.getVehicle().getRegNo(), time(10, 0)).fee() == 0.0);
            check(facility.exit(visitor.getVehicle().getRegNo(), time(10, 0)).fee() == 0.0);
        });
        test("one-day pass does not waive EV charging energy", () -> {
            ParkingFacility facility = new ParkingFacility(new int[]{1});
            LocalDateTime entry = time(8, 0);
            ParkingTicket ticket = facility.enter(new ElectricCar("TN01AA0017"), entry,
                    new OneDayPass(LocalDate.from(entry)));
            check(facility.exit(ticket.getVehicle().getRegNo(), time(9, 0), 1).fee() == 12.0);
        });
        test("electric vehicle prefers charging slot and bills energy", () -> {
            ParkingFacility facility = new ParkingFacility(new int[]{2}, new int[][]{{1}});
            ParkingTicket ticket = facility.enter(new ElectricCar("TN01AA0011"), time(8, 0), null);
            check(ticket.getSlot().isCharging());
            check(facility.exit(ticket.getVehicle().getRegNo(), time(9, 0), 2).fee() == 54.0);
        });
        test("e-bike flat rate and charge cost", () -> {
            ParkingFacility facility = new ParkingFacility(new int[]{1}, new int[][]{{0}});
            ParkingTicket ticket = facility.enter(new EBike("TN01AA0012"), time(8, 0), null);
            check(facility.exit(ticket.getVehicle().getRegNo(), time(9, 1), 1).fee() == 37.0);
        });
        test("delivery van doubles car rate in peak hours", () -> {
            ParkingFacility facility = new ParkingFacility(new int[]{1});
            ParkingTicket ticket = facility.enter(new DeliveryVan("TN01AA0013"), time(9, 0), null);
            check(facility.exit(ticket.getVehicle().getRegNo(), time(10, 0)).fee() == 60.0);
        });
        test("ten-hour daily maximum applies", () -> {
            ParkingFacility facility = new ParkingFacility(new int[]{1});
            ParkingTicket ticket = facility.enter(new Car("TN01AA0016"), time(0, 0), null);
            check(facility.exit(ticket.getVehicle().getRegNo(), time(11, 0)).fee() == 300.0);
        });
        test("reused slot and empty lookup", () -> {
            ParkingFacility facility = new ParkingFacility(new int[]{1});
            ParkingTicket first = facility.enter(new Car("TN01AA0014"), time(8, 0), null);
            facility.exit(first.getVehicle().getRegNo(), time(8, 0));
            ParkingTicket second = facility.enter(new Car("TN01AA0015"), time(8, 0), null);
            check(second.getSlot() == first.getSlot());
            expect(IllegalArgumentException.class,
                    () -> facility.exit("TN01AA0099", time(9, 0)));
        });
        System.out.println("Passed " + passed + " tests.");
    }

    private static LocalDateTime time(int hour, int minute) {
        return LocalDateTime.of(2026, 11, 14, hour, minute);
    }

    private static void test(String name, Runnable test) {
        test.run();
        passed++;
        System.out.println("PASS: " + name);
    }

    private static void check(boolean condition) {
        if (!condition) {
            throw new AssertionError("Expected condition to be true");
        }
    }

    private static void expect(Class<? extends RuntimeException> expected, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException exception) {
            if (expected.isInstance(exception)) {
                return;
            }
            throw new AssertionError("Expected " + expected.getSimpleName()
                    + " but got " + exception.getClass().getSimpleName(), exception);
        }
        throw new AssertionError("Expected " + expected.getSimpleName());
    }
}
