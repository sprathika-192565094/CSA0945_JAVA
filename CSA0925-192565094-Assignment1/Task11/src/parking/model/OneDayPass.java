package parking.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public final class OneDayPass implements ParkingPass {
    private final LocalDate validDate;

    public OneDayPass(LocalDate validDate) {
        this.validDate = Objects.requireNonNull(validDate, "Valid date cannot be null");
    }

    @Override
    public boolean isValidAt(LocalDateTime time) {
        return validDate.equals(Objects.requireNonNull(time).toLocalDate());
    }
}
