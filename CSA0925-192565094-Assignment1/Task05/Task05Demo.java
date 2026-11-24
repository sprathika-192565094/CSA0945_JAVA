import java.util.concurrent.atomic.AtomicLong;

public class Task05Demo {
    static final class ParkingTicket {
        private static final AtomicLong NEXT_ID = new AtomicLong(1);
        private final long id = NEXT_ID.getAndIncrement();

        long getId() {
            return id;
        }
    }

    public static void main(String[] args) {
        ParkingTicket northFacilityTicket = new ParkingTicket();
        ParkingTicket southFacilityTicket = new ParkingTicket();
        System.out.println(northFacilityTicket.getId());
        System.out.println(southFacilityTicket.getId());
    }
}
