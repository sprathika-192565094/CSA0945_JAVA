import java.util.concurrent.CountDownLatch;

public class TokenCounter {
    private final int[] buffer = new int[5];
    private int count;
    private int in;
    private int out;
    private final StringBuilder countTrace = new StringBuilder();

    public synchronized void put(int token) throws InterruptedException {
        while (count == buffer.length) {
            wait();
        }
        buffer[in] = token;
        in = (in + 1) % buffer.length;
        count++;
        countTrace.append(count).append(' ');
        notifyAll();
    }

    public synchronized int take() throws InterruptedException {
        while (count == 0) {
            wait();
        }
        int token = buffer[out];
        out = (out + 1) % buffer.length;
        count--;
        countTrace.append(count).append(' ');
        notifyAll();
        return token;
    }

    private synchronized String getCountTrace() {
        return countTrace.toString().trim();
    }

    private static final class Wallet {
        private final int id;
        private int balance;

        Wallet(int id, int balance) {
            this.id = id;
            this.balance = balance;
        }

        void transferSafelyTo(Wallet target, int amount) {
            Wallet first = id < target.id ? this : target;
            Wallet second = id < target.id ? target : this;
            synchronized (first) {
                synchronized (second) {
                    if (balance >= amount) {
                        balance -= amount;
                        target.balance += amount;
                    }
                }
            }
        }

        void transferUnsafelyTo(Wallet target, int amount, CountDownLatch start) {
            try {
                start.await();
                synchronized (this) {
                    Thread.sleep(50);
                    synchronized (target) {
                        if (balance >= amount) {
                            balance -= amount;
                            target.balance += amount;
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        if (args.length > 0 && args[0].equals("--deadlock-demo")) {
            showDeadlock();
            return;
        }
        TokenCounter counter = new TokenCounter();
        int[] collected = new int[2];
        Thread cook = new Thread(() -> {
            try {
                for (int token = 1; token <= 20; token++) {
                    counter.put(token);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        Thread[] students = new Thread[2];
        for (int i = 0; i < students.length; i++) {
            final int id = i;
            students[i] = new Thread(() -> {
                try {
                    for (int j = 0; j < 10; j++) {
                        collected[id] += counter.take();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        cook.start();
        for (Thread student : students) {
            student.start();
        }
        cook.join();
        for (Thread student : students) {
            student.join();
        }
        System.out.println("Sum of tokens collected: " + (collected[0] + collected[1])
                + " (expected 210)");
        System.out.println("Counter size after each operation: " + counter.getCountTrace());

        Wallet first = new Wallet(1, 1_000);
        Wallet second = new Wallet(2, 1_000);
        Thread a = new Thread(() -> first.transferSafelyTo(second, 100));
        Thread b = new Thread(() -> second.transferSafelyTo(first, 100));
        a.start();
        b.start();
        a.join();
        b.join();
        System.out.println("Ordered wallet transfers completed");
    }

    private static void showDeadlock() throws InterruptedException {
        Wallet first = new Wallet(1, 1_000);
        Wallet second = new Wallet(2, 1_000);
        CountDownLatch start = new CountDownLatch(1);
        Thread a = new Thread(() -> first.transferUnsafelyTo(second, 10, start), "A-to-B");
        Thread b = new Thread(() -> second.transferUnsafelyTo(first, 10, start), "B-to-A");
        a.setDaemon(true);
        b.setDaemon(true);
        a.start();
        b.start();
        start.countDown();
        Thread.sleep(200);
        System.out.println("Both opposite-order transfers still alive: " + (a.isAlive() && b.isAlive()));
    }
}
