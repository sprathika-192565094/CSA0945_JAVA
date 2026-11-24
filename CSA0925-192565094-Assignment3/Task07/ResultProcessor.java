import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ResultProcessor {
    private static final int FILE_COUNT = 10_000;

    private static final class Outcome {
        final boolean success;
        final String message;

        Outcome(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    public static void main(String[] args) throws Exception {
        Path inputDirectory = Files.createTempDirectory("java-assignment-scores");
        try {
            List<Path> files = createInputs(inputDirectory);
            for (int poolSize : new int[]{1, 2, 4, 8}) {
                process(files, poolSize);
            }
        } finally {
            try (var paths = Files.walk(inputDirectory)) {
                paths.sorted((left, right) -> right.compareTo(left))
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException e) {
                                throw new RuntimeException("Could not remove temporary input " + path, e);
                            }
                        });
            }
        }
    }

    private static List<Path> createInputs(Path directory) throws IOException {
        List<Path> files = new ArrayList<>(FILE_COUNT);
        for (int i = 0; i < FILE_COUNT; i++) {
            Path file = directory.resolve(String.format("score-%05d.txt", i));
            if (i == 9_998) {
                Files.writeString(file, "corrupt score data");
            } else if (i != 9_999) {
                Files.writeString(file, "STUDENT-" + i + ",80");
            }
            files.add(file);
        }
        return files;
    }

    private static void process(List<Path> files, int poolSize) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);
        ExecutorCompletionService<Outcome> completions = new ExecutorCompletionService<>(executor);
        ConcurrentLinkedQueue<Outcome> results = new ConcurrentLinkedQueue<>();
        long start = System.nanoTime();

        try {
            for (Path file : files) {
                Callable<Outcome> job = () -> {
                    Outcome outcome = processOne(file);
                    results.add(outcome);
                    return outcome;
                };
                completions.submit(job);
            }

            for (int i = 0; i < files.size(); i++) {
                Future<Outcome> completed = completions.take();
                try {
                    completed.get();
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    String reason = cause == null ? "unknown task failure"
                            : cause.getClass().getSimpleName() + ": " + cause.getMessage();
                    results.add(new Outcome(false, "Unexpected task failure: " + reason));
                }
            }
        } finally {
            executor.shutdown();
            if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                executor.shutdownNow();
                throw new IllegalStateException("Worker pool did not terminate");
            }
        }

        int succeeded = 0;
        int failed = 0;
        for (Outcome outcome : results) {
            if (outcome.success) {
                succeeded++;
            } else {
                failed++;
            }
        }
        long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        System.out.printf("Pool %d: success=%d, failure=%d, total=%d, elapsed=%d ms%n",
                poolSize, succeeded, failed, results.size(), elapsedMillis);
        results.stream().filter(outcome -> !outcome.success).limit(2)
                .forEach(outcome -> System.out.println("  Failure: " + outcome.message));
    }

    private static Outcome processOne(Path file) {
        try {
            String[] fields = Files.readString(file).trim().split(",");
            if (fields.length != 2 || fields[0].isBlank()) {
                return new Outcome(false, file.getFileName() + ": invalid score record");
            }
            int score = Integer.parseInt(fields[1]);
            if (score < 0 || score > 100) {
                return new Outcome(false, file.getFileName() + ": score outside 0..100");
            }
            return new Outcome(true, fields[0] + ": " + gradeFor(score));
        } catch (IOException | NumberFormatException | SecurityException e) {
            return new Outcome(false, file.getFileName() + ": " + e.getClass().getSimpleName());
        }
    }

    private static String gradeFor(int score) {
        return score >= 50 ? "PASS" : "FAIL";
    }
}
