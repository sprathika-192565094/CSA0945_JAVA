package parking.model;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Objects;

public final class MonthlyPass implements ParkingPass {
    private final YearMonth validMonth;

    public MonthlyPass(YearMonth validMonth) {
        this.validMonth = Objects.requireNonNull(validMonth, "Valid month cannot be null");
    }

    @Override
    public boolean isValidAt(LocalDateTime time) {
        return validMonth.equals(YearMonth.from(Objects.requireNonNull(time)));
    }
}
