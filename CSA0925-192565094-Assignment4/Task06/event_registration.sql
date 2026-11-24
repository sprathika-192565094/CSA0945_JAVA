CREATE DATABASE IF NOT EXISTS event_db;
USE event_db;

CREATE TABLE IF NOT EXISTS registrations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(20) NOT NULL,
    student_name VARCHAR(100) NOT NULL,
    event_name VARCHAR(80) NOT NULL,
    registered_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_registration (student_id, event_name)
);

INSERT INTO registrations (student_id, student_name, event_name, registered_at)
VALUES
('1924261001', 'Ananya', 'Dance Fest', '2026-11-17 09:00:00'),
('1924261002', 'Rohan', 'Hackathon', '2026-11-17 09:15:00')
ON DUPLICATE KEY UPDATE student_name = VALUES(student_name), event_name = VALUES(event_name);
