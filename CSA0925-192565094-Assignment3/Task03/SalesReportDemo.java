import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SalesReportDemo {
    private static final List<Path> STORE_FILES = List.of(
            Path.of("Task03\\store-a.csv"),
            Path.of("Task03\\store-b.csv"));
    private static final Path OUTPUT = Path.of("Task03\\daily-summary.txt");

    public static void main(String[] args) throws IOException {
        boolean injectFailure = args.length > 0 && args[0].equals("--fail");
        Path failingPath = injectFailure ? Path.of("Task03\\missing-store.csv") : STORE_FILES.get(1);
        List<Path> inputs = new ArrayList<>(STORE_FILES);
        inputs.set(1, failingPath);

        boolean manualSucceeded = runManual(inputs, Path.of("Task03\\manual-summary.txt"));
        boolean resourcesSucceeded = runWithResources(inputs, OUTPUT);
        if (!injectFailure && manualSucceeded && resourcesSucceeded) {
            boolean sameReport = Files.readString(Path.of("Task03\\manual-summary.txt"))
                    .equals(Files.readString(OUTPUT));
            System.out.println("Reports match: " + sameReport);
        }
    }

    private static void generateManual(List<Path> inputs, Path output) throws IOException {
        BufferedWriter writer = null;
        Exception failure = null;
        try {
            writer = Files.newBufferedWriter(output);
            double total = 0;
            for (Path input : inputs) {
                System.out.println("Reading " + input);
                BufferedReader reader = null;
                Exception inputFailure = null;
                try {
                    reader = Files.newBufferedReader(input);
                    String line;
                    while ((line = reader.readLine()) != null) {
                        total += Double.parseDouble(line.trim());
                    }
                } catch (IOException | NumberFormatException e) {
                    inputFailure = e;
                    throw e;
                } finally {
                    closeManually(reader, inputFailure);
                }
            }
            writer.write("Total sales: " + total);
            writer.newLine();
        } catch (IOException | NumberFormatException e) {
            failure = e;
            System.err.println("Manual version failed: " + e.getMessage());
            throw e;
        } finally {
            closeManually(writer, failure);
        }
    }

    private static void generateWithResources(List<Path> inputs, Path output) throws IOException {
        double total = 0;
        for (Path input : inputs) {
            System.out.println("Reading " + input);
            try (BufferedReader reader = Files.newBufferedReader(input)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    total += Double.parseDouble(line.trim());
                }
            } catch (IOException | NumberFormatException e) {
                System.err.println("Try-with-resources failed for " + input + ": " + e.getMessage());
                throw e;
            }
        }

        try (BufferedWriter writer = Files.newBufferedWriter(output)) {
            writer.write("Total sales: " + total);
            writer.newLine();
        }
    }

    private static boolean runManual(List<Path> inputs, Path output) {
        try {
            generateManual(inputs, output);
            return true;
        } catch (IOException | NumberFormatException e) {
            System.err.println("Manual report incomplete: " + e.getMessage());
            return false;
        }
    }

    private static boolean runWithResources(List<Path> inputs, Path output) {
        try {
            generateWithResources(inputs, output);
            return true;
        } catch (IOException | NumberFormatException e) {
            System.err.println("Try-with-resources report incomplete: " + e.getMessage());
            return false;
        }
    }

    private static void closeManually(Closeable resource, Exception failure) throws IOException {
        if (resource == null) {
            return;
        }
        try {
            resource.close();
        } catch (IOException closeFailure) {
            if (failure == null) {
                throw closeFailure;
            }
            failure.addSuppressed(closeFailure);
        }
    }
}
