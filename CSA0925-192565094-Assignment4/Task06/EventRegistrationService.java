import java.util.List;

public class EventRegistrationService {
    private final RegistrationDAO dao;

    public EventRegistrationService(RegistrationDAO dao) {
        this.dao = dao;
    }

    public String registerStudent(String studentId, String studentName, String eventName) {
        if (studentId == null || studentId.trim().isEmpty()) {
            return "Student ID is required.";
        }
        if (studentName == null || studentName.trim().isEmpty()) {
            return "Student name is required.";
        }
        if (eventName == null || eventName.trim().isEmpty()) {
            return "Event selection is required.";
        }

        if (dao.exists(studentId.trim(), eventName.trim())) {
            return "Duplicate registration: the student is already registered for this event.";
        }

        Registration registration = new Registration(
            studentId.trim(),
            studentName.trim(),
            eventName.trim(),
            java.time.LocalDateTime.now()
        );

        if (dao.insert(registration)) {
            return "Registration successful for " + studentName.trim() + ".";
        }
        return "Registration failed. Please try again.";
    }

    public List<Registration> getRegistrations(String filterEvent) {
        return dao.findAll(filterEvent);
    }
}
