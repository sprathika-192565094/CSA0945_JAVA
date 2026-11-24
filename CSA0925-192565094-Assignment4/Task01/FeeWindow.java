import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class FeeWindow extends Frame {
    private final TextField regNo = new TextField(20);
    private final TextField amount = new TextField(20);
    private final Button pay = new Button("Pay");
    private final Button clear = new Button("Clear");
    private final Button receipt = new Button("Receipt");
    private final TextArea status = new TextArea(6, 30);
    private String lastReceipt = "No receipt yet.";

    public FeeWindow() {
        super("Student Fee Payment");
        setLayout(new BorderLayout(10, 10));

        Panel form = new Panel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        form.add(new Label("Reg No:"));
        form.add(regNo);
        form.add(new Label("Amount:"));
        form.add(amount);

        Panel buttons = new Panel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttons.add(pay);
        buttons.add(clear);
        buttons.add(receipt);

        status.setEditable(false);
        status.setText("Ready to process a payment.");
        add(form, BorderLayout.NORTH);
        add(buttons, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);

        pay.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handlePay();
            }
        });

        clear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                regNo.setText("");
                amount.setText("");
                status.setText("Fields cleared.");
            }
        });

        receipt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                status.setText(lastReceipt);
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        setSize(new Dimension(460, 220));
        setLocationRelativeTo(null);
    }

    private void handlePay() {
        FeeService.PaymentResult result = FeeService.processPayment(regNo.getText(), amount.getText());
        if (result.isSuccess()) {
            lastReceipt = FeeService.createReceipt(regNo.getText().trim(), result.getAmount());
            status.setText(result.getMessage() + "\n" + lastReceipt);
        } else {
            status.setText(result.getMessage());
        }
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new FeeWindow().setVisible(true));
    }
}
