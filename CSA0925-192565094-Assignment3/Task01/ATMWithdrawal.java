import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ATMWithdrawal {
    private static class InsufficientFundsException extends Exception {
        private static final long serialVersionUID = 1L;

        InsufficientFundsException(String message) {
            super(message);
        }
    }

    public static void main(String[] args) {
        String cardFile = args.length > 2 ? args[2] : "Task01\\card-reader.txt";
        try {
            if (args.length > 0 && args[0].equals("--uncaught")) {
                throw new IllegalStateException("Demonstration of an uncaught exception");
            }
            if (args.length < 2) {
                throw new IllegalArgumentException(
                        "Usage: java ATMWithdrawal <account-number> <amount> [card-file]");
            }

            String[] accountData = Files.readString(Path.of(cardFile)).trim().split(",");
            if (accountData.length != 2) {
                throw new IllegalArgumentException("Card data must contain account,balance");
            }
            String accountNumber = args[0];
            if (!accountNumber.equals(accountData[0])) {
                throw new IllegalArgumentException("Account number does not match the card");
            }
            double balance = Double.parseDouble(accountData[1]);
            double amount = Double.parseDouble(args[1]);

            if (amount <= 0) {
                throw new IllegalArgumentException("Withdrawal amount must be positive");
            }
            if (amount > balance) {
                throw new InsufficientFundsException("Insufficient funds for account " + accountNumber);
            }

            System.out.printf("Dispensed %.2f for account %s%n", amount, accountNumber);
        } catch (NumberFormatException e) {
            System.err.println("Invalid number: " + e.getMessage());
        } catch (InsufficientFundsException e) {
            System.err.println("Declined: " + e.getMessage());
        } catch (IOException | SecurityException e) {
            System.err.println("Card-reader failure: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid request: " + e.getMessage());
        } finally {
            System.out.println("ATM session closed");
        }
    }
}
