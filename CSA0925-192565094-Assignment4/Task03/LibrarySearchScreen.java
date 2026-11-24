import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class LibrarySearchScreen extends JFrame {
    private final JTextField searchField = new JTextField(18);
    private final JComboBox<String> categoryBox = new JComboBox<>(new String[]{"All", "Books", "Journals", "Research"});
    private final JTextArea resultsArea = new JTextArea(14, 40);
    private final JLabel statusLabel = new JLabel("Ready");

    public LibrarySearchScreen() {
        super("Campus Library Search");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("Category:"));
        searchPanel.add(categoryBox);
        JButton searchButton = new JButton("Search");
        searchPanel.add(searchButton);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        JButton prevButton = new JButton("Prev");
        JButton nextButton = new JButton("Next");
        JButton reserveButton = new JButton("Reserve");
        bottomPanel.add(prevButton);
        bottomPanel.add(nextButton);
        bottomPanel.add(reserveButton);

        resultsArea.setEditable(false);
        resultsArea.setText("1. Introduction to Java\n2. GUI Design in Swing\n3. Database and JDBC\n4. Research Methods\n5. Algorithms and Data Structures");
        JScrollPane scrollPane = new JScrollPane(resultsArea);

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        statusPanel.add(statusLabel, BorderLayout.CENTER);

        add(searchPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        add(statusPanel, BorderLayout.EAST);

        searchButton.addActionListener((ActionEvent e) -> {
            statusLabel.setText("Searching in " + categoryBox.getSelectedItem() + " for: " + searchField.getText());
            resultsArea.append("\nSearch performed.");
        });

        prevButton.addActionListener((ActionEvent e) -> statusLabel.setText("Previous result selected."));
        nextButton.addActionListener((ActionEvent e) -> statusLabel.setText("Next result selected."));
        reserveButton.addActionListener((ActionEvent e) -> statusLabel.setText("Reservation request sent."));

        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LibrarySearchScreen().setVisible(true));
    }
}
