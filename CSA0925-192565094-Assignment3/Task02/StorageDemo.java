import java.io.IOException;

class ExamException extends Exception {
    private static final long serialVersionUID = 1L;

    ExamException(String message) {
        super(message);
    }

    ExamException(String message, Throwable cause) {
        super(message, cause);
    }
}

class SubmissionStorageException extends ExamException {
    private static final long serialVersionUID = 1L;

    SubmissionStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}

class ExamWindowClosedException extends ExamException {
    private static final long serialVersionUID = 1L;

    ExamWindowClosedException() {
        super("The examination window is closed");
    }
}

class StudentNotRegisteredException extends ExamException {
    private static final long serialVersionUID = 1L;

    StudentNotRegisteredException() {
        super("The student is not registered for this course");
    }
}

class Storage {
    void save(String fileName, long sizeKb) throws SubmissionStorageException {
        try {
            if (sizeKb > 500) {
                throw new IOException("Disk quota exceeded");
            }
            System.out.println("Saved " + fileName);
        } catch (IOException e) {
            throw new SubmissionStorageException("Could not store " + fileName, e);
        }
    }
}

class SubmissionService {
    private final Storage storage = new Storage();

    void submit(String student, boolean windowOpen, boolean registered,
            String fileName, long sizeKb) throws ExamException {
        if (!windowOpen) {
            throw new ExamWindowClosedException();
        }
        if (!registered) {
            throw new StudentNotRegisteredException();
        }
        storage.save(fileName, sizeKb);
    }
}

class ExamController {
    private final SubmissionService service = new SubmissionService();

    void submit(String student, boolean windowOpen, boolean registered,
            String fileName, long sizeKb) {
        try {
            service.submit(student, windowOpen, registered, fileName, sizeKb);
        } catch (ExamException e) {
            System.out.println("Error: " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("Cause: " + e.getCause().getMessage());
                e.printStackTrace(System.out);
            } else {
                System.out.println("Student message: " + e.getMessage());
            }
        }
    }
}

public class StorageDemo {
    public static void main(String[] args) {
        ExamController controller = new ExamController();
        controller.submit("STU-01", true, true, "answer01.pdf", 120);
        controller.submit("STU-01", false, true, "late-answer.pdf", 120);
        controller.submit("STU-02", true, false, "unregistered-answer.pdf", 120);
        controller.submit("STU-01", true, true, "answer02.pdf", 800);
    }
}
