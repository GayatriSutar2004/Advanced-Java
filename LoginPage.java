package buspass;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginPage extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton, registerButton;

    // Database details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/bus_pass";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Tejas@45";

    // Constructor
    public LoginPage() {
        initUI();
    }

    private void initUI() {
        setTitle("Login");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);
        getContentPane().setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Bus Pass Login", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBounds(100, 20, 200, 30);
        add(titleLabel);

        JLabel userLabel = new JLabel("User Name:");
        userLabel.setBounds(10, 70, 200, 30);
        add(userLabel);

        usernameField = new JTextField();
        usernameField.setBounds(100, 70, 200, 30);
        usernameField.setToolTipText("Username");
        add(usernameField);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setBounds(10, 110, 200, 30);
        add(passLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(100, 110, 200, 30);
        passwordField.setToolTipText("Password");
        add(passwordField);

        loginButton = new JButton("Login");
        loginButton.setBounds(100, 160, 90, 35);
        add(loginButton);

        registerButton = new JButton("Register");
        registerButton.setBounds(210, 160, 90, 35);
        add(registerButton);

        loginButton.addActionListener(e -> login());

        registerButton.addActionListener(e -> {
            dispose();
            new RegisterPage(); // Ensure RegisterPage has a no-arg constructor
        });

        setVisible(true);
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and password cannot be empty.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT username, password, name, role FROM `user` WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    String name = rs.getString("name");
                    String role = rs.getString("role");

                    if (storedPassword.equals(password)) {
                        JOptionPane.showMessageDialog(this, "Welcome " + name + "!", "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                        dispose();

                        if (role.equalsIgnoreCase("admin")) {
                            new AdminPassRecords();
                        } else {
                            new UserDashboard(username);
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "Incorrect password.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "User not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginPage::new);
    }
}
