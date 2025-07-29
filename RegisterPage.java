package buspass;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class RegisterPage extends JFrame {

    final JTextField usernameField, nameField, ageField, addressField;
    final JPasswordField passwordField;
    final JComboBox<String> genderCombo;
    final JRadioButton userRadio, adminRadio;
    final JButton registerBtn, backBtn;

    private static final String DB_URL = "jdbc:oracle:thin:@localhost:1521:xe";
    private static final String DB_USER = "System";
    private static final String DB_PASSWORD = "system";

    public RegisterPage() {
        setTitle("Register");
        setSize(400, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        getContentPane().setBackground(Color.WHITE);

        JLabel heading = new JLabel("Register");
        heading.setFont(new Font("Arial", Font.BOLD, 24));
        heading.setBounds(140, 20, 120, 30);
        add(heading);

        JLabel userLabel = new JLabel("User Name:");
        userLabel.setBounds(10, 70, 200, 30);
        add(userLabel);

        usernameField = new JTextField();
        usernameField.setBounds(100, 70, 200, 30);
        add(usernameField);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setBounds(10, 110, 200, 30);
        add(passLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(100, 110, 200, 30);
        add(passwordField);

        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setBounds(10, 150, 200, 30);
        add(nameLabel);

        nameField = new JTextField();
        nameField.setBounds(100, 150, 200, 30);
        add(nameField);

        JLabel ageLabel = new JLabel("Age:");
        ageLabel.setBounds(10, 190, 200, 30);
        add(ageLabel);

        ageField = new JTextField();
        ageField.setBounds(100, 190, 200, 30);
        add(ageField);

        JLabel addressLabel = new JLabel("Address:");
        addressLabel.setBounds(10, 230, 200, 30);
        add(addressLabel);

        addressField = new JTextField();
        addressField.setBounds(100, 230, 200, 30);
        add(addressField);

        JLabel genderLabel = new JLabel("Gender:");
        genderLabel.setBounds(10, 270, 200, 30);
        add(genderLabel);

        genderCombo = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        genderCombo.setBounds(100, 270, 200, 30);
        add(genderCombo);

        userRadio = new JRadioButton("User");
        adminRadio = new JRadioButton("Admin");
        ButtonGroup group = new ButtonGroup();
        group.add(userRadio);
        group.add(adminRadio);
        userRadio.setBounds(100, 310, 80, 30);
        adminRadio.setBounds(200, 310, 80, 30);
        add(userRadio);
        add(adminRadio);
        userRadio.setSelected(true);

        registerBtn = new JButton("Register");
        registerBtn.setBounds(100, 360, 90, 35);
        add(registerBtn);

        backBtn = new JButton("Back");
        backBtn.setBounds(210, 360, 90, 35);
        add(backBtn);

        registerBtn.addActionListener(e -> registerUser());
        backBtn.addActionListener(e -> {
            new LoginPage().setVisible(true);
            dispose();
        });

        setVisible(true);
    }

    private void registerUser() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String name = nameField.getText().trim();
        String ageStr = ageField.getText().trim();
        String address = addressField.getText().trim();
        String gender = (String) genderCombo.getSelectedItem();

        if (username.isEmpty() || password.isEmpty() || name.isEmpty() || ageStr.isEmpty() || address.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Age must be a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (isUsernameTaken(username)) {
            JOptionPane.showMessageDialog(this, "Username already exists.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String role = adminRadio.isSelected() ? "admin" : "user";

        saveUserToDatabase(username, password, name, age, gender, address, role);

        JOptionPane.showMessageDialog(this, "Registered Successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        new LoginPage().setVisible(true);
        dispose();
    }

    private boolean isUsernameTaken(String username) {
        String query = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error occurred.", "Error", JOptionPane.ERROR_MESSAGE);
            return true;
        }
    }

    private void saveUserToDatabase(String username, String password, String name, int age, String gender,
                                    String address, String role) {
        String query = "INSERT INTO users (username, password, name, age, gender, address, role) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, name);
            stmt.setInt(4, age);
            stmt.setString(5, gender);
            stmt.setString(6, address);
            stmt.setString(7, role);
            stmt.executeUpdate();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving user to database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    static {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
