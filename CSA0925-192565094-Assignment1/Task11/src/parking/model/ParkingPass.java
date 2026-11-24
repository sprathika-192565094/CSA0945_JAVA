package parking.model;

import java.time.LocalDateTime;

public interface ParkingPass {
    boolean isValidAt(LocalDateTime time);
}
