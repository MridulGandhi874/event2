package user;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import events.Events; // Added missing import

public class Users {

    private ArrayList<User> userList;
    private Events events;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/event_management";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "your_password";

    private class User {
        String username;
        String phone;
        String eventTitle;

        User(String username, String phone, String eventTitle) {
            this.username = username;
            this.phone = phone;
            this.eventTitle = eventTitle;
        }
    }

    public Users(Events events) {
        this.events = events;
        userList = new ArrayList<>();
        loadFromDatabase();
    }

    public void userOperations() {
        JFrame frame = new JFrame("User Operations");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(450, 300);
        frame.setLayout(new GridLayout(5, 1));

        JButton addButton = new JButton("Add User");
        JButton removeButton = new JButton("Remove User");
        JButton viewButton = new JButton("View All Users");
        JButton backButton = new JButton("Back to Main Menu");

        addButton.addActionListener(e -> showAddUserDialog());
        removeButton.addActionListener(e -> showRemoveUserDialog());
        viewButton.addActionListener(e -> viewAllUsers());
        backButton.addActionListener(e -> frame.dispose());

        frame.add(addButton);
        frame.add(removeButton);
        frame.add(viewButton);
        frame.add(backButton);

        frame.setVisible(true);
    }

    private void showAddUserDialog() {
        JDialog dialog = new JDialog((Frame) null, "Add User", true);
        dialog.setSize(450, 300);
        dialog.setLayout(new GridLayout(4, 2));

        JTextField usernameField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField eventTitleField = new JTextField();

        dialog.add(new JLabel("Username:"));
        dialog.add(usernameField);
        dialog.add(new JLabel("Phone Number:"));
        dialog.add(phoneField);
        dialog.add(new JLabel("Event Title:"));
        dialog.add(eventTitleField);

        JButton submitButton = new JButton("Add");
        submitButton.addActionListener(e -> {
            try {
                String username = usernameField.getText();
                String phone = phoneField.getText();
                String eventTitle = eventTitleField.getText();
                if (events.eventExists(eventTitle)) {
                    addUser(username, phone, eventTitle);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Event does not exist!");
                }
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage());
            }
        });

        dialog.add(submitButton);
        dialog.setVisible(true);
    }

    private void showRemoveUserDialog() {
        JDialog dialog = new JDialog((Frame) null, "Remove User", true);
        dialog.setSize(450, 300);
        dialog.setLayout(new GridLayout(2, 2));

        JTextField usernameField = new JTextField();
        dialog.add(new JLabel("Username:"));
        dialog.add(usernameField);

        JButton submitButton = new JButton("Remove");
        submitButton.addActionListener(e -> {
            removeUser(usernameField.getText());
            dialog.dispose();
        });

        dialog.add(submitButton);
        dialog.setVisible(true);
    }

    public void addUser(String username, String phone, String eventTitle) throws IllegalArgumentException {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (phone == null || !phone.matches("\\d{10}")) {
            throw new IllegalArgumentException("Phone number must be 10 digits");
        }
        if (eventTitle == null || eventTitle.trim().isEmpty()) {
            throw new IllegalArgumentException("Event title cannot be empty");
        }
        if (!events.eventExists(eventTitle)) {
            throw new IllegalArgumentException("Event does not exist");
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO users (username, phone, event_title) VALUES (?, ?, ?)")) {
            stmt.setString(1, username);
            stmt.setString(2, phone);
            stmt.setString(3, eventTitle);
            stmt.executeUpdate();
            userList.add(new User(username, phone, eventTitle));
            JOptionPane.showMessageDialog(null, "User " + username + " added successfully for event: " + eventTitle);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error adding user: " + e.getMessage());
        }
    }

    public void removeUser(String username) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE username = ?")) {
            stmt.setString(1, username);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                userList.removeIf(user -> user.username.equals(username));
                JOptionPane.showMessageDialog(null, "User " + username + " removed successfully");
            } else {
                JOptionPane.showMessageDialog(null, "User " + username + " not found");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error removing user: " + e.getMessage());
        }
    }

    public void removeUsersByEvent(String eventTitle) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE event_title = ?")) {
            stmt.setString(1, eventTitle);
            stmt.executeUpdate();
            userList.removeIf(user -> user.eventTitle.equals(eventTitle));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error removing users by event: " + e.getMessage());
        }
    }

    public void viewAllUsers() {
        if (userList.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No users registered yet!");
            return;
        }
        StringBuilder sb = new StringBuilder("Registered Users:\n");
        for (User user : userList) {
            sb.append("Name: ").append(user.username)
                    .append(", Phone: ").append(user.phone)
                    .append(", Event: ").append(user.eventTitle).append("\n");
        }
        JOptionPane.showMessageDialog(null, sb.toString());
    }

    private void loadFromDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM users")) {
            userList.clear();
            while (rs.next()) {
                userList.add(new User(rs.getString("username"), rs.getString("phone"), rs.getString("event_title")));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading users: " + e.getMessage());
        }
    }
}