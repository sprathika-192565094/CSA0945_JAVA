import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class ScannerStations {
    private static class ThreadStation extends Thread {
        private final int scripts;
        private final AtomicInteger total;

        ThreadStation(String name, int scripts, AtomicInteger total) {
            super(name);
            this.scripts = scripts;
            this.total = total;
        }

        @Override
        public void run() {
            scan(scripts, total);
        }
    }

    private static class RunnableStation implements Runnable {
        private final int scripts;
        private final AtomicInteger total;

        RunnableStation(int scripts, AtomicInteger total) {
            this.scripts = scripts;
            this.total = total;
        }

        @Override
        public void run() {
            scan(scripts, total);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        if (args.length > 0 && args[0].equals("--run-inline")) {
            ThreadStation inline = new ThreadStation("Inline-Station", 5, new AtomicInteger());
            inline.run();
            System.out.println("run() returned; no separate thread was started");
            return;
        }
        if (args.length > 0 && args[0].equals("--daemon-demo")) {
            Thread daemon = new Thread(() -> {
                try {
                    Thread.sleep(2_000);
                    System.out.println("Daemon station finished");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Daemon-Station");
            daemon.setDaemon(true);
            daemon.start();
            System.out.println("Main ends without joining the daemon");
            return;
        }

        AtomicInteger total = new AtomicInteger();
        List<Thread> stations = new ArrayList<>();
        stations.add(new ThreadStation("Station-1", 10, total));
        stations.add(new Thread(new RunnableStation(20, total), "Station-2"));
        stations.add(new ThreadStation("Station-3", 30, total));
        stations.add(new Thread(new RunnableStation(40, total), "Station-4"));

        if (args.length > 0 && args[0].equals("--priority-demo")) {
            for (int i = 0; i < stations.size(); i++) {
                stations.get(i).setPriority(Thread.MIN_PRIORITY + i * 3);
            }
        }
        for (Thread station : stations) {
            station.start();
        }
        for (Thread station : stations) {
            station.join();
        }
        for (Thread station : stations) {
            System.out.println(station.getName() + " alive after join: " + station.isAlive());
        }
        System.out.println("Total scripts scanned: " + total.get());
    }

    private static void scan(int scripts, AtomicInteger total) {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(10, 101));
            total.addAndGet(scripts);
            System.out.println(Thread.currentThread().getName() + " scanned " + scripts);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println(Thread.currentThread().getName() + " was interrupted");
        }
    }
}
