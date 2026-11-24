import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RegistrationDAO {
    private final Map<String, Registration> inMemory = new LinkedHashMap<>();

    public boolean insert(Registration registration) {
        if (DatabaseConfig.isMySqlAvailable()) {
            return insertInMySql(registration);
        }
        String key = registration.getStudentId() + "::" + registration.getEventName();
        if (inMemory.containsKey(key)) {
            return false;
        }
        inMemory.put(key, registration);
        return true;
    }

    public List<Registration> findAll(String filterEvent) {
        if (DatabaseConfig.isMySqlAvailable()) {
            return findAllFromMySql(filterEvent);
        }
        List<Registration> rows = new ArrayList<>();
        for (Registration registration : inMemory.values()) {
            if ("All".equalsIgnoreCase(filterEvent) || filterEvent == null || filterEvent.isEmpty() || registration.getEventName().equalsIgnoreCase(filterEvent)) {
                rows.add(registration);
            }
        }
        return rows;
    }

    public boolean exists(String studentId, String eventName) {
        if (DatabaseConfig.isMySqlAvailable()) {
            return existsInMySql(studentId, eventName);
        }
        String key = studentId + "::" + eventName;
        return inMemory.containsKey(key);
    }

    private boolean insertInMySql(Registration registration) {
        String sql = "INSERT INTO registrations (student_id, student_name, event_name, registered_at) VALUES (?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(DatabaseConfig.URL, DatabaseConfig.USER, DatabaseConfig.PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, registration.getStudentId());
            statement.setString(2, registration.getStudentName());
            statement.setString(3, registration.getEventName());
            statement.setString(4, registration.getRegisteredAt().toString());
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private List<Registration> findAllFromMySql(String filterEvent) {
        List<Registration> rows = new ArrayList<>();
        String sql = "SELECT student_id, student_name, event_name, registered_at FROM registrations";
        if (filterEvent != null && !filterEvent.isEmpty() && !"All".equalsIgnoreCase(filterEvent)) {
            sql += " WHERE event_name = ?";
        }

        try (Connection connection = DriverManager.getConnection(DatabaseConfig.URL, DatabaseConfig.USER, DatabaseConfig.PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (filterEvent != null && !filterEvent.isEmpty() && !"All".equalsIgnoreCase(filterEvent)) {
                statement.setString(1, filterEvent);
            }
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                rows.add(new Registration(
                    resultSet.getString("student_id"),
                    resultSet.getString("student_name"),
                    resultSet.getString("event_name"),
                    LocalDateTime.parse(resultSet.getString("registered_at"))
                ));
            }
            return rows;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return Collections.emptyList();
        }
    }

    private boolean existsInMySql(String studentId, String eventName) {
        String sql = "SELECT 1 FROM registrations WHERE student_id = ? AND event_name = ? LIMIT 1";
        try (Connection connection = DriverManager.getConnection(DatabaseConfig.URL, DatabaseConfig.USER, DatabaseConfig.PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, studentId);
            statement.setString(2, eventName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
