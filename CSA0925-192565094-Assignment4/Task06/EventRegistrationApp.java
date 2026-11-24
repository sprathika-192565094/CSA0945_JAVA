import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

public class EventRegistrationApp extends JFrame {
    private final EventRegistrationService service = new EventRegistrationService(new RegistrationDAO());
    private final JTextField studentIdField = new JTextField(12);
    private final JTextField studentNameField = new JTextField(18);
    private final JComboBox<String> eventCombo = new JComboBox<>(new String[]{"Dance Fest", "Tech Expo", "Cultural Night", "Hackathon"});
    private final JComboBox<String> filterCombo = new JComboBox<>(new String[]{"All", "Dance Fest", "Tech Expo", "Cultural Night", "Hackathon"});
    private final DefaultTableModel tableModel = new DefaultTableModel(
        new Object[]{"Student ID", "Student Name", "Event", "Registered At"}, 0
    );
    private final JTable registrationsTable = new JTable(tableModel);
    private final JLabel statusLabel = new JLabel("Ready");

    public EventRegistrationApp() {
        super("Campus Event Registration");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        formPanel.setBorder(BorderFactory.createTitledBorder("Register Event"));
        formPanel.add(new JLabel("Student ID:"));
        formPanel.add(studentIdField);
        formPanel.add(new JLabel("Name:"));
        formPanel.add(studentNameField);
        formPanel.add(new JLabel("Event:"));
        formPanel.add(eventCombo);

        JButton registerButton = new JButton("Register");
        formPanel.add(registerButton);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filter"));
        filterPanel.add(new JLabel("Event:"));
        filterPanel.add(filterCombo);
        JButton refreshButton = new JButton("Refresh");
        filterPanel.add(refreshButton);

        JScrollPane scrollPane = new JScrollPane(registrationsTable);
        scrollPane.setPreferredSize(new Dimension(700, 300));

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(statusLabel, BorderLayout.CENTER);

        add(formPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(filterPanel, BorderLayout.SOUTH);
        add(southPanel, BorderLayout.EAST);

        registerButton.addActionListener(e -> registerStudent());
        refreshButton.addActionListener(e -> refreshRegistrationsAsync());
        filterCombo.addActionListener(e -> refreshRegistrationsAsync());

        pack();
        setLocationRelativeTo(null);
        refreshRegistrationsAsync();
    }

    private void registerStudent() {
        String studentId = studentIdField.getText();
        String studentName = studentNameField.getText();
        String eventName = (String) eventCombo.getSelectedItem();

        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                return service.registerStudent(studentId, studentName, eventName);
            }

            @Override
            protected void done() {
                try {
                    statusLabel.setText(get());
                    studentIdField.setText("");
                    studentNameField.setText("");
                    refreshRegistrationsAsync();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    statusLabel.setText("Registration interrupted.");
                } catch (ExecutionException ex) {
                    statusLabel.setText("Registration failed: " + ex.getCause().getMessage());
                }
            }
        };
        worker.execute();
    }

    private void refreshRegistrationsAsync() {
        String selected = (String) filterCombo.getSelectedItem();
        SwingWorker<List<Registration>, Void> worker = new SwingWorker<List<Registration>, Void>() {
            @Override
            protected List<Registration> doInBackground() {
                return service.getRegistrations(selected);
            }

            @Override
            protected void done() {
                try {
                    tableModel.setRowCount(0);
                    for (Registration registration : get()) {
                        tableModel.addRow(new Object[]{
                            registration.getStudentId(),
                            registration.getStudentName(),
                            registration.getEventName(),
                            registration.getRegisteredAt()
                        });
                    }
                    statusLabel.setText("Loaded " + tableModel.getRowCount() + " registrations.");
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    statusLabel.setText("Load was interrupted.");
                } catch (ExecutionException ex) {
                    statusLabel.setText("Could not load registrations.");
                }
            }
        };
        worker.execute();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EventRegistrationApp().setVisible(true));
    }
}
