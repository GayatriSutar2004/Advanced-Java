
package buspass;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import javax.swing.table.DefaultTableModel;

public class AdminPassRecords extends JFrame {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/bus_pass";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Tejas@45";

    public AdminPassRecords() {
        setTitle("Admin - All Pass Records");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JLabel titleLabel = new JLabel("All Pass Records", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(70, 130, 180));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());

        // Column names (modified to match available columns)
        String[] columnNames = {
                "Pass ID", "User ID", "Pass Type",
                "Duration", "Applied Date", "Amount", "Status"
        };

        // Table model
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };

        JTable passTable = new JTable(model);
        passTable.setFont(new Font("Arial", Font.PLAIN, 14));
        passTable.setRowHeight(25);
        passTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

        // Make table scrollable
        JScrollPane scrollPane = new JScrollPane(passTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        JLabel filterLabel = new JLabel("Filter by Status:");
        filterLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        String[] statusOptions = { "All", "Applied", "Approved", "Paid", "Rejected", "Cancelled" };
        JComboBox<String> statusFilter = new JComboBox<>(statusOptions);
        statusFilter.setFont(new Font("Arial", Font.PLAIN, 14));

        JButton filterButton = new JButton("Apply Filter");
        filterButton.setFont(new Font("Arial", Font.PLAIN, 14));
        filterButton.setBackground(new Color(70, 130, 180));
        filterButton.setForeground(Color.WHITE);

        filterPanel.add(filterLabel);
        filterPanel.add(statusFilter);
        filterPanel.add(filterButton);

        mainPanel.add(filterPanel, BorderLayout.NORTH);
        mainPanel.add(tablePanel, BorderLayout.CENTER);

        // Load data button
        JButton refreshButton = new JButton("Refresh Data");
        refreshButton.setFont(new Font("Arial", Font.BOLD, 14));
        refreshButton.setBackground(new Color(70, 130, 180));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.addActionListener(e -> loadPassData(model, "All"));

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(refreshButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);

        // Load data initially
        loadPassData(model, "All");

        // Filter button action
        filterButton.addActionListener(e -> {
            String selectedStatus = (String) statusFilter.getSelectedItem();
            loadPassData(model, selectedStatus);
        });

        setVisible(true);
    }

    private void loadPassData(DefaultTableModel model, String statusFilter) {
        model.setRowCount(0); // Clear existing data

        String query = "SELECT up.id, u.username, pt.name as pass_name, " +
                "pt.duration, up.applied_date, up.amount_paid, up.status " +
                "FROM user_passes up " +
                "JOIN pass_types pt ON up.pass_type_id = pt.pass_type_id " +
                "JOIN user u ON up.user_id = u.username ";

        if (!statusFilter.equals("All")) {
            query += "WHERE up.status = '" + statusFilter + "' ";
        }

        query += "ORDER BY up.applied_date DESC";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");

            while (rs.next()) {
                Object[] row = {
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("pass_name"),
                        rs.getString("duration"),
                        sdf.format(rs.getDate("applied_date")),
                        String.format("₹%.2f", rs.getDouble("amount_paid")),
                        rs.getString("status")
                };
                model.addRow(row);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading pass records: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                new AdminPassRecords();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}