public final class DatabaseConfig {
    public static final String URL = "jdbc:mysql://localhost:3306/event_db";
    public static final String USER = "root";
    public static final String PASSWORD = "";
    public static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    private DatabaseConfig() {
    }

    public static boolean isMySqlAvailable() {
        try {
            Class.forName(DRIVER);
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }
}
