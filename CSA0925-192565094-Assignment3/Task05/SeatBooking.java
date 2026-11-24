import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class SeatBooking {
    private final boolean[] booked;
    private final Object[] seatLocks;
    private int bookingsMade;

    SeatBooking(int seats) {
        booked = new boolean[seats];
        seatLocks = new Object[seats];
        for (int i = 0; i < seats; i++) {
            seatLocks[i] = new Object();
        }
    }

    synchronized boolean bookWithMethodLock(int seat) {
        if (booked[seat]) {
            return false;
        }
        booked[seat] = true;
        bookingsMade++;
        return true;
    }

    boolean bookWithSeatLock(int seat) {
        synchronized (seatLocks[seat]) {
            if (booked[seat]) {
                return false;
            }
            booked[seat] = true;
            bookingsMade++;
            return true;
        }
    }

    boolean bookUnsafelyForDemonstration(int seat, CyclicBarrier checked) throws Exception {
        if (booked[seat]) {
            return false;
        }
        checked.await();
        booked[seat] = true;
        bookingsMade++;
        return true;
    }

    int getBookingsMade() {
        return bookingsMade;
    }

    public static void main(String[] args) throws Exception {
        demonstrateRace();
        System.out.println("Synchronized method seats sold: " + runSafeBooking(false));
        System.out.println("Synchronized block seats sold: " + runSafeBooking(true));
        measureTenThousandBookings();
    }

    private static void demonstrateRace() throws Exception {
        int seats = 10;
        int users = 100;
        SeatBooking screen = new SeatBooking(seats);
        CyclicBarrier[] checked = new CyclicBarrier[seats];
        AtomicInteger successes = new AtomicInteger();
        Thread[] customers = new Thread[users];
        for (int seat = 0; seat < seats; seat++) {
            checked[seat] = new CyclicBarrier(users / seats);
        }
        for (int i = 0; i < users; i++) {
            final int seat = i % seats;
            customers[i] = new Thread(() -> {
                try {
                    if (screen.bookUnsafelyForDemonstration(seat, checked[seat])) {
                        successes.incrementAndGet();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            customers[i].start();
        }
        for (Thread customer : customers) {
            customer.join();
        }
        System.out.println("Unsynchronized successful attempts: " + successes.get()
                + " for " + seats + " seats");
    }

    private static int runSafeBooking(boolean useBlock) throws InterruptedException {
        int seats = 10;
        SeatBooking screen = new SeatBooking(seats);
        Thread[] customers = new Thread[100];
        for (int i = 0; i < customers.length; i++) {
            final int seat = i % seats;
            customers[i] = new Thread(() -> {
                if (useBlock) {
                    screen.bookWithSeatLock(seat);
                } else {
                    screen.bookWithMethodLock(seat);
                }
            });
            customers[i].start();
        }
        for (Thread customer : customers) {
            customer.join();
        }
        return screen.getBookingsMade();
    }

    private static void measureTenThousandBookings() {
        final int attempts = 10_000;
        long methodNanos = timeUniqueSeats(attempts, false);
        long blockNanos = timeUniqueSeats(attempts, true);
        System.out.printf("10,000 uncontended unique-seat attempts: method=%.3f ms, block=%.3f ms%n",
                methodNanos / 1_000_000.0, blockNanos / 1_000_000.0);
    }

    private static long timeUniqueSeats(int attempts, boolean useBlock) {
        SeatBooking screen = new SeatBooking(attempts);
        long start = System.nanoTime();
        for (int seat = 0; seat < attempts; seat++) {
            if (useBlock) {
                screen.bookWithSeatLock(seat);
            } else {
                screen.bookWithMethodLock(seat);
            }
        }
        return System.nanoTime() - start;
    }
}
