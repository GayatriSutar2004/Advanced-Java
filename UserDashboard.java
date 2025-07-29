
package buspass;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UserDashboard extends JFrame {

    private final String userId;
    private JPanel contentPanel;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/bus_pass";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Tejas@45";

    public UserDashboard(String userId) {
        this.userId = userId;

        setTitle("User Dashboard - " + userId);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Left panel (button menu)
        JPanel leftPanel = new JPanel();
        leftPanel.setPreferredSize(new Dimension(250, getHeight()));
        leftPanel.setBackground(new Color(220, 240, 250));
        leftPanel.setLayout(new GridLayout(12, 1, 10, 10));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        Font btnFont = new Font("Arial", Font.PLAIN, 16);
        Dimension btnSize = new Dimension(200, 50);

        JButton applyBtn = new JButton("Apply for New Pass");
        styleButton(applyBtn, btnFont, btnSize);

        JButton renewBtn = new JButton("Renew Pass");
        styleButton(renewBtn, btnFont, btnSize);

        JButton paymentBtn = new JButton("Make Payment");
        styleButton(paymentBtn, btnFont, btnSize);

        JButton cancelBtn = new JButton("Cancel Pass");
        styleButton(cancelBtn, btnFont, btnSize);

        JButton viewBtn = new JButton("View Pass Details");
        styleButton(viewBtn, btnFont, btnSize);

        JButton logoutBtn = new JButton("Logout");
        styleButton(logoutBtn, btnFont, btnSize);

        leftPanel.add(applyBtn);
        leftPanel.add(renewBtn);
        leftPanel.add(paymentBtn);
        leftPanel.add(cancelBtn);
        leftPanel.add(viewBtn);
        leftPanel.add(logoutBtn);

        add(leftPanel, BorderLayout.WEST);

        // Right panel (main content)
        contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        add(contentPanel, BorderLayout.CENTER);

        // Welcome panel as default
        showWelcomePanel();

        // Action listeners
        applyBtn.addActionListener(e -> setContentPanel(new ApplyPassPanel()));
        renewBtn.addActionListener(e -> renewPass());
        paymentBtn.addActionListener(e -> makePayment());
        cancelBtn.addActionListener(e -> cancelPass());
        viewBtn.addActionListener(e -> viewPassDetails());
        logoutBtn.addActionListener(e -> logout());

        setVisible(true);
    }

    private void styleButton(JButton button, Font font, Dimension size) {
        button.setFont(font);
        button.setPreferredSize(size);
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
    }

    private void showWelcomePanel() {
        JPanel welcomePanel = new JPanel(new BorderLayout());
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        JLabel welcomeLabel = new JLabel("Welcome, " + userId + "!", JLabel.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 36));
        welcomeLabel.setForeground(new Color(70, 130, 180));

        JLabel infoLabel = new JLabel("Please select an option from the left menu", JLabel.CENTER);
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 18));

        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        centerPanel.add(welcomeLabel);
        centerPanel.add(infoLabel);

        welcomePanel.add(centerPanel, BorderLayout.CENTER);
        setContentPanel(welcomePanel);
    }

    private void setContentPanel(JPanel panel) {
        contentPanel.removeAll();
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?", "Confirm Logout",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginPage().setVisible(true);
        }
    }

    private class ApplyPassPanel extends JPanel {
        public ApplyPassPanel() {
            setLayout(new BorderLayout(10, 10));
            setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            JLabel title = new JLabel("Available Pass Types", JLabel.CENTER);
            title.setFont(new Font("Arial", Font.BOLD, 24));
            title.setForeground(new Color(70, 130, 180));
            add(title, BorderLayout.NORTH);

            JPanel passContainer = new JPanel();
            passContainer.setLayout(new BoxLayout(passContainer, BoxLayout.Y_AXIS));
            passContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            JScrollPane scrollPane = new JScrollPane(passContainer);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            add(scrollPane, BorderLayout.CENTER);

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT * FROM pass_types")) {

                while (rs.next()) {
                    int passId = rs.getInt("pass_type_id");
                    String name = rs.getString("name");
                    String duration = rs.getString("duration");
                    double fare = rs.getDouble("base_fare");
                    String discount = rs.getString("discount_info");

                    JPanel card = createPassCard(passId, name, duration, fare, discount);
                    passContainer.add(card);
                    passContainer.add(Box.createRigidArea(new Dimension(0, 15)));
                }

            } catch (SQLException ex) {
                showErrorDialog("Error loading pass types: " + ex.getMessage());
            }
        }

        private JPanel createPassCard(int passId, String name, String duration, double fare, String discount) {
            JPanel card = new JPanel(new BorderLayout(10, 10));
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(10, 10, 10, 10),
                    BorderFactory.createLineBorder(new Color(70, 130, 180), 2)));
            card.setBackground(new Color(240, 248, 255));
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

            JTextArea infoArea = new JTextArea(
                    String.format("%s (%s)\nFare: ₹%.2f\nDiscount: %s", name, duration, fare, discount));
            infoArea.setEditable(false);
            infoArea.setFont(new Font("Arial", Font.PLAIN, 14));
            infoArea.setBackground(new Color(240, 248, 255));
            card.add(infoArea, BorderLayout.CENTER);

            JButton applyBtn = new JButton("Apply");
            applyBtn.setPreferredSize(new Dimension(120, 40));
            applyBtn.setFont(new Font("Arial", Font.BOLD, 14));
            applyBtn.setBackground(new Color(70, 130, 180));
            applyBtn.setForeground(Color.WHITE);
            card.add(applyBtn, BorderLayout.EAST);

            applyBtn.addActionListener(e -> applyForPass(passId, fare));

            return card;
        }

        private void applyForPass(int passId, double baseFare) {
            double finalFare = baseFare;
            int age = 0;
            String gender = "";

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                    PreparedStatement stmt = conn.prepareStatement(
                            "SELECT gender, age FROM user WHERE username = ?")) {
                stmt.setString(1, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    gender = rs.getString("gender");
                    age = rs.getInt("age");
                }
            } catch (SQLException ex) {
                showErrorDialog("Error fetching user details: " + ex.getMessage());
                return;
            }

            // Apply discount logic
            if ("Female".equalsIgnoreCase(gender)) {
                finalFare *= 0.5;
            } else if (age > 75) {
                finalFare = 0;
            } else if (age > 65) {
                finalFare *= 0.5;
            }

            // Check if user already has an active pass
            if (hasActivePass(passId)) {
                JOptionPane.showMessageDialog(this,
                        "You already have an active pass of this type.",
                        "Application Failed", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Insert the application
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                    PreparedStatement stmt = conn.prepareStatement(
                            "INSERT INTO user_passes (user_id, pass_type_id, status, amount_paid, applied_date) " +
                                    "VALUES (?, ?, 'Applied', ?, ?)")) {
                stmt.setString(1, userId);
                stmt.setInt(2, passId);
                stmt.setDouble(3, finalFare);
                stmt.setDate(4, new java.sql.Date(new Date().getTime()));
                stmt.executeUpdate();

                JOptionPane.showMessageDialog(this,
                        "Pass applied successfully!\nAmount to pay: ₹" + String.format("%.2f", finalFare),
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                showErrorDialog("Error applying for pass: " + ex.getMessage());
            }
        }

        private boolean hasActivePass(int passId) {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                    PreparedStatement stmt = conn.prepareStatement(
                            "SELECT 1 FROM user_passes WHERE user_id = ? AND pass_type_id = ? AND status IN ('Applied', 'Paid')")) {
                stmt.setString(1, userId);
                stmt.setInt(2, passId);
                return stmt.executeQuery().next();
            } catch (SQLException ex) {
                return false;
            }
        }
    }

    private void renewPass() {
        JPanel renewPanel = new JPanel(new BorderLayout(10, 10));
        renewPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Renew Bus Pass", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(new Color(70, 130, 180));
        renewPanel.add(title, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout());
        JTextArea infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setFont(new Font("Arial", Font.PLAIN, 14));
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(infoArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        content.add(scrollPane, BorderLayout.CENTER);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT up.id, pt.name, pt.duration, pt.base_fare, up.amount_paid, up.status, up.pass_type_id, up.applied_date "
                                +
                                "FROM user_passes up JOIN pass_types pt ON up.pass_type_id = pt.pass_type_id " +
                                "WHERE up.user_id = ? AND up.status = 'Paid' ORDER BY up.applied_date DESC LIMIT 1")) {

            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int userPassId = rs.getInt("id");
                int passTypeId = rs.getInt("pass_type_id");
                String name = rs.getString("name");
                String duration = rs.getString("duration");
                double fare = rs.getDouble("base_fare");
                Date appliedDate = rs.getDate("applied_date");

                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
                infoArea.setText(String.format(
                        "Pass Name   : %s\n" +
                                "Duration    : %s\n" +
                                "Last Paid   : ₹%.2f\n" +
                                "Applied Date: %s\n" +
                                "Status      : %s\n\n" +
                                "Click below to renew this pass.",
                        name, duration, rs.getDouble("amount_paid"),
                        sdf.format(appliedDate), rs.getString("status")));

                JButton renewBtn = new JButton("Renew Now");
                renewBtn.setFont(new Font("Arial", Font.BOLD, 14));
                renewBtn.setPreferredSize(new Dimension(150, 40));
                renewBtn.setBackground(new Color(70, 130, 180));
                renewBtn.setForeground(Color.WHITE);

                JPanel btnPanel = new JPanel();
                btnPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
                btnPanel.add(renewBtn);
                content.add(btnPanel, BorderLayout.SOUTH);

                renewBtn.addActionListener(e -> {
                    try (PreparedStatement insertStmt = conn.prepareStatement(
                            "INSERT INTO user_passes (user_id, pass_type_id, status, amount_paid, applied_date) " +
                                    "VALUES (?, ?, 'Applied', ?, ?)")) {
                        insertStmt.setString(1, userId);
                        insertStmt.setInt(2, passTypeId);
                        insertStmt.setDouble(3, fare);
                        insertStmt.setDate(4, new java.sql.Date(new Date().getTime()));
                        insertStmt.executeUpdate();

                        JOptionPane.showMessageDialog(this,
                                "Pass renewal requested. Please proceed to payment.",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        renewPass(); // refresh panel
                    } catch (SQLException ex) {
                        showErrorDialog("Renewal failed: " + ex.getMessage());
                    }
                });

            } else {
                infoArea.setText("No pass available for renewal.");
            }

        } catch (SQLException ex) {
            showErrorDialog("Error checking pass renewal: " + ex.getMessage());
        }

        renewPanel.add(content, BorderLayout.CENTER);
        setContentPanel(renewPanel);
    }

    private void makePayment() {
        String query = "SELECT up.id, pt.name, pt.duration, pt.base_fare, up.status, up.amount_paid " +
                "FROM user_passes up JOIN pass_types pt ON up.pass_type_id = pt.pass_type_id " +
                "WHERE up.user_id = ? AND up.status = 'Applied' ORDER BY up.applied_date DESC LIMIT 1";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int userPassId = rs.getInt("id");
                String passName = rs.getString("name");
                String duration = rs.getString("duration");
                double amount = rs.getDouble("amount_paid");

                JPanel paymentPanel = new JPanel(new BorderLayout(10, 10));
                paymentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

                JLabel title = new JLabel("Payment Details", JLabel.CENTER);
                title.setFont(new Font("Arial", Font.BOLD, 24));
                title.setForeground(new Color(70, 130, 180));
                paymentPanel.add(title, BorderLayout.NORTH);

                JPanel detailsPanel = new JPanel(new GridLayout(5, 2, 10, 10));
                detailsPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

                addDetailRow(detailsPanel, "Pass Name:", passName);
                addDetailRow(detailsPanel, "Duration:", duration);
                addDetailRow(detailsPanel, "Amount to Pay:", String.format("₹%.2f", amount));

                JLabel paymentMethodLabel = new JLabel("Payment Method:");
                paymentMethodLabel.setFont(new Font("Arial", Font.BOLD, 14));
                detailsPanel.add(paymentMethodLabel);

                JComboBox<String> paymentMethod = new JComboBox<>(
                        new String[] { "Credit Card", "Debit Card", "Net Banking", "UPI" });
                detailsPanel.add(paymentMethod);

                paymentPanel.add(detailsPanel, BorderLayout.CENTER);

                JButton payButton = new JButton("Confirm Payment");
                payButton.setPreferredSize(new Dimension(180, 45));
                payButton.setFont(new Font("Arial", Font.BOLD, 16));
                payButton.setBackground(new Color(70, 130, 180));
                payButton.setForeground(Color.WHITE);

                JPanel buttonPanel = new JPanel();
                buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
                buttonPanel.add(payButton);
                paymentPanel.add(buttonPanel, BorderLayout.SOUTH);

                setContentPanel(paymentPanel);

                payButton.addActionListener(e -> {
                    // Create a panel to hold both text and image
                    JPanel panel = new JPanel(new BorderLayout(10, 10));

                    // Add your confirmation text
                    JLabel message = new JLabel(
                            "Confirm payment of ₹" + String.format("%.2f", amount) + "?",
                            JLabel.CENTER);
                    panel.add(message, BorderLayout.NORTH);

                    // Add QR code image (replace with your actual image path)
                    try {
                        ImageIcon qrIcon = new ImageIcon("/resources/qr.jpg"); // Update this path
                        JLabel imageLabel = new JLabel(qrIcon);
                        imageLabel.setHorizontalAlignment(JLabel.CENTER);
                        panel.add(imageLabel, BorderLayout.CENTER);
                    } catch (Exception ex) {
                        panel.add(new JLabel("QR code not available", JLabel.CENTER));
                    }

                    // Show the custom dialog
                    int confirm = JOptionPane.showConfirmDialog(
                            this,
                            panel,
                            "Confirm Payment",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.PLAIN_MESSAGE);

                    if (confirm == JOptionPane.YES_OPTION) {
                        processPayment(userPassId, passName, amount);
                    }
                });

            } else {
                JOptionPane.showMessageDialog(this,
                        "No pass found to make payment for.",
                        "Information", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException ex) {
            showErrorDialog("Error loading pass details: " + ex.getMessage());
        }
    }

    private void addDetailRow(JPanel panel, String label, String value) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(lbl);

        JLabel val = new JLabel(value);
        val.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(val);
    }

    private void processPayment(int userPassId, String passName, double amount) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE user_passes SET status = 'Paid', payment_date = ? WHERE id = ?")) {

            stmt.setDate(1, new java.sql.Date(new Date().getTime()));
            stmt.setInt(2, userPassId);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(this,
                        "Payment successful for " + passName,
                        "Success", JOptionPane.INFORMATION_MESSAGE);

                // Show receipt
                showReceipt(passName, amount);
            } else {
                showErrorDialog("Error processing payment.");
            }

        } catch (SQLException ex) {
            showErrorDialog("Error processing payment: " + ex.getMessage());
        }
    }

    private void showReceipt(String passName, double amount) {
        JPanel receiptPanel = new JPanel(new BorderLayout(10, 10));
        receiptPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Payment Receipt", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(new Color(70, 130, 180));
        receiptPanel.add(title, BorderLayout.NORTH);

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy, hh:mm a");
        String receiptText = String.format(
                "Pass Name: %s\n" +
                        "Amount Paid: ₹%.2f\n" +
                        "Payment Date: %s\n" +
                        "Payment Status: Successful\n\n" +
                        "Thank you for your payment!",
                passName, amount, sdf.format(new Date()));

        JTextArea receiptArea = new JTextArea(receiptText);
        receiptArea.setEditable(false);
        receiptArea.setFont(new Font("Arial", Font.PLAIN, 16));
        receiptArea.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        JScrollPane scrollPane = new JScrollPane(receiptArea);
        receiptPanel.add(scrollPane, BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Arial", Font.BOLD, 14));
        closeButton.setBackground(new Color(70, 130, 180));
        closeButton.setForeground(Color.WHITE);
        closeButton.addActionListener(e -> viewPassDetails());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        receiptPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPanel(receiptPanel);
    }

    private void cancelPass() {
        JPanel cancelPanel = new JPanel(new BorderLayout(10, 10));
        cancelPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Cancel Bus Pass", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(new Color(70, 130, 180));
        cancelPanel.add(title, BorderLayout.NORTH);

        JTextArea infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setFont(new Font("Arial", Font.PLAIN, 14));
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(infoArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel content = new JPanel(new BorderLayout());
        content.add(scrollPane, BorderLayout.CENTER);

        // Store the pass details that we'll need in the action listener
        final int[] userPassIdHolder = new int[1];
        final String[] nameHolder = new String[1];
        final String[] durationHolder = new String[1];
        final String[] statusHolder = new String[1];
        final Date[] appliedDateHolder = new Date[1];

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT up.id, pt.name, pt.duration, up.status, up.applied_date " +
                                "FROM user_passes up JOIN pass_types pt ON up.pass_type_id = pt.pass_type_id " +
                                "WHERE up.user_id = ? AND up.status IN ('Applied', 'Paid') ORDER BY up.applied_date DESC LIMIT 1")) {

            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                userPassIdHolder[0] = rs.getInt("id");
                nameHolder[0] = rs.getString("name");
                durationHolder[0] = rs.getString("duration");
                statusHolder[0] = rs.getString("status");
                appliedDateHolder[0] = rs.getDate("applied_date");

                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
                infoArea.setText(String.format(
                        "Pass Name   : %s\n" +
                                "Duration    : %s\n" +
                                "Applied Date: %s\n" +
                                "Status      : %s\n\n" +
                                "Click below to cancel this pass.",
                        nameHolder[0], durationHolder[0], sdf.format(appliedDateHolder[0]), statusHolder[0]));

                JButton cancelBtn = new JButton("Cancel Pass");
                cancelBtn.setFont(new Font("Arial", Font.BOLD, 14));
                cancelBtn.setPreferredSize(new Dimension(150, 40));
                cancelBtn.setBackground(new Color(220, 20, 60));
                cancelBtn.setForeground(Color.WHITE);

                JPanel btnPanel = new JPanel();
                btnPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
                btnPanel.add(cancelBtn);
                content.add(btnPanel, BorderLayout.SOUTH);

                cancelBtn.addActionListener(e -> {
                    int confirm = JOptionPane.showConfirmDialog(this,
                            "Are you sure you want to cancel this pass?\nThis action cannot be undone.",
                            "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        // Create a new connection for the update operation
                        try (Connection newConn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                                PreparedStatement updateStmt = newConn.prepareStatement(
                                        "UPDATE user_passes SET status = 'Cancelled', cancelled_date = ? WHERE id = ?")) {

                            updateStmt.setDate(1, new java.sql.Date(new Date().getTime()));
                            updateStmt.setInt(2, userPassIdHolder[0]);
                            updateStmt.executeUpdate();

                            JOptionPane.showMessageDialog(this,
                                    "Pass cancelled successfully.",
                                    "Success", JOptionPane.INFORMATION_MESSAGE);
                            cancelPass(); // refresh view
                        } catch (SQLException ex) {
                            showErrorDialog("Cancellation failed: " + ex.getMessage());
                        }
                    }
                });

            } else {
                infoArea.setText("No active pass available to cancel.");
            }

        } catch (SQLException ex) {
            showErrorDialog("Error retrieving pass data: " + ex.getMessage());
        }

        cancelPanel.add(content, BorderLayout.CENTER);
        setContentPanel(cancelPanel);
    }

    private void viewPassDetails() {
        JPanel viewPanel = new JPanel(new BorderLayout(10, 10));
        viewPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Your Bus Pass Details", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(new Color(70, 130, 180));
        viewPanel.add(title, BorderLayout.NORTH);

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Arial", Font.PLAIN, 14));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        viewPanel.add(scrollPane, BorderLayout.CENTER);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // First check if payment_date column exists
            boolean hasPaymentDate = false;
            try (ResultSet rs = conn.getMetaData().getColumns(null, null, "user_passes", "payment_date")) {
                hasPaymentDate = rs.next();
            }

            String query = "SELECT pt.name, pt.duration, up.status, up.applied_date, up.amount_paid" +
                    (hasPaymentDate ? ", up.payment_date" : "") + " " +
                    "FROM user_passes up JOIN pass_types pt ON up.pass_type_id = pt.pass_type_id " +
                    "WHERE up.user_id = ? ORDER BY up.applied_date DESC";

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, userId);
                ResultSet rs = stmt.executeQuery();

                StringBuilder sb = new StringBuilder();
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
                int count = 0;

                while (rs.next()) {
                    count++;
                    sb.append("Pass #").append(count).append(":\n");
                    sb.append("Pass Name   : ").append(rs.getString("name")).append("\n");
                    sb.append("Duration    : ").append(rs.getString("duration")).append("\n");
                    sb.append("Status      : ").append(rs.getString("status")).append("\n");
                    sb.append("Applied Date: ").append(sdf.format(rs.getDate("applied_date"))).append("\n");

                    if (hasPaymentDate) {
                        Date paymentDate = rs.getDate("payment_date");
                        if (paymentDate != null) {
                            sb.append("Payment Date: ").append(sdf.format(paymentDate)).append("\n");
                        }
                    }

                    sb.append("Amount Paid : ₹").append(String.format("%.2f", rs.getDouble("amount_paid")))
                            .append("\n");
                    sb.append("------------------------------------------------\n");
                }

                if (count == 0) {
                    sb.append("You have not applied for any passes yet.");
                }

                textArea.setText(sb.toString());
            }

        } catch (SQLException ex) {
            showErrorDialog("Error loading pass details: " + ex.getMessage());
        }

        setContentPanel(viewPanel);
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new UserDashboard("user1");
        });
    }
}