import java.util.Objects;

public class FeeService {
    public static final class PaymentResult {
        private final boolean success;
        private final String message;
        private final double amount;

        public PaymentResult(boolean success, String message, double amount) {
            this.success = success;
            this.message = message;
            this.amount = amount;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public double getAmount() {
            return amount;
        }
    }

    public static PaymentResult processPayment(String regNo, String amountText) {
        String reg = regNo == null ? "" : regNo.trim();
        String amountValue = amountText == null ? "" : amountText.trim();

        if (reg.isEmpty()) {
            return new PaymentResult(false, "Registration number is required.", 0.0);
        }

        double amount;
        try {
            amount = Double.parseDouble(amountValue);
        } catch (NumberFormatException ex) {
            return new PaymentResult(false, "Amount must be a valid number.", 0.0);
        }

        if (amount <= 0.0) {
            return new PaymentResult(false, "Amount must be greater than zero.", 0.0);
        }

        return new PaymentResult(true, "Payment successful for " + reg + ".", amount);
    }

    public static String createReceipt(String regNo, double amount) {
        Objects.requireNonNull(regNo, "Registration number must not be null");
        return "Receipt\nReg No: " + regNo + "\nAmount: Rs " + String.format("%.2f", amount);
    }
}
