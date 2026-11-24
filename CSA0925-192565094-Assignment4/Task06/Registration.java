import java.time.LocalDateTime;

public class Registration {
    private final String studentId;
    private final String studentName;
    private final String eventName;
    private final LocalDateTime registeredAt;

    public Registration(String studentId, String studentName, String eventName, LocalDateTime registeredAt) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.eventName = eventName;
        this.registeredAt = registeredAt;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getEventName() {
        return eventName;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }
}
