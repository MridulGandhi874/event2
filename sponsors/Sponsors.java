package sponsors;

import events.Events;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;

public class Sponsors {

    private ArrayList<Sponsor> sponsorList;
    private Events events;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/event_management";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "your_password";

    private class Sponsor {
        String name;
        double amount;
        String eventTitle;

        Sponsor(String name, double amount, String eventTitle) {
            this.name = name;
            this.amount = amount;
            this.eventTitle = eventTitle;
        }
    }

    public Sponsors(Events events) {
        this.events = events;
        sponsorList = new ArrayList<>();
        loadFromDatabase();
    }

    public void sponsorOperations() {
        JFrame frame = new JFrame("Sponsor Operations");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(450, 300);
        frame.setLayout(new GridLayout(5, 1));

        JButton addButton = new JButton("Add Sponsor");
        JButton removeButton = new JButton("Remove Sponsor");
        JButton viewButton = new JButton("View All Sponsors");
        JButton totalButton = new JButton("View Total Sponsorship");
        JButton backButton = new JButton("Back to Main Menu");

        addButton.addActionListener(e -> showAddSponsorDialog());
        removeButton.addActionListener(e -> showRemoveSponsorDialog());
        viewButton.addActionListener(e -> viewAllSponsors());
        totalButton.addActionListener(e -> JOptionPane.showMessageDialog(null, "TotalExec Summary: Total Sponsorship: $" + getTotalSponsorship()));
        backButton.addActionListener(e -> frame.dispose());

        frame.add(addButton);
        frame.add(removeButton);
        frame.add(viewButton);
        frame.add(totalButton);
        frame.add(backButton);

        frame.setVisible(true);
    }

    private void showAddSponsorDialog() {
        JDialog dialog = new JDialog((Frame) null, "Add Sponsor", true);
        dialog.setSize(450, 300);
        dialog.setLayout(new GridLayout(4, 2));

        JTextField nameField = new JTextField();
        JTextField amountField = new JTextField();
        JTextField eventTitleField = new JTextField();

        dialog.add(new JLabel("Sponsor Name:"));
        dialog.add(nameField);
        dialog.add(new JLabel("Amount:"));
        dialog.add(amountField);
        dialog.add(new JLabel("Event Title:"));
        dialog.add(eventTitleField);

        JButton submitButton = new JButton("Add");
        submitButton.addActionListener(e -> {
            try {
                String name = nameField.getText();
                double amount = Double.parseDouble(amountField.getText());
                String eventTitle = eventTitleField.getText();
                if (events.eventExists(eventTitle)) {
                    addSponsor(name, amount, eventTitle);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Event does not exist!");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid amount!");
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage());
            }
        });

        dialog.add(submitButton);
        dialog.setVisible(true);
    }

    private void showRemoveSponsorDialog() {
        JDialog dialog = new JDialog((Frame) null, "Remove Sponsor", true);
        dialog.setSize(450, 300);
        dialog.setLayout(new GridLayout(2, 2));

        JTextField nameField = new JTextField();
        dialog.add(new JLabel("Sponsor Name:"));
        dialog.add(nameField);

        JButton submitButton = new JButton("Remove");
        submitButton.addActionListener(e -> {
            removeSponsor(nameField.getText());
            dialog.dispose();
        });

        dialog.add(submitButton);
        dialog.setVisible(true);
    }

    public void addSponsor(String name, double amount, String eventTitle) throws IllegalArgumentException {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Sponsor name cannot be empty");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (eventTitle == null || eventTitle.trim().isEmpty()) {
            throw new IllegalArgumentException("Event title cannot be empty");
        }
        if (!events.eventExists(eventTitle)) {
            throw new IllegalArgumentException("Event does not exist");
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO sponsors (name, amount, event_title) VALUES (?, ?, ?)")) {
            stmt.setString(1, name);
            stmt.setDouble(2, amount);
            stmt.setString(3, eventTitle);
            stmt.executeUpdate();
            sponsorList.add(new Sponsor(name, amount, eventTitle));
            JOptionPane.showMessageDialog(null, "Sponsor " + name + " added with $" + amount + " for " + eventTitle);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error adding sponsor: " + e.getMessage());
        }
    }

    public void removeSponsor(String name) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM sponsors WHERE name = ?")) {
            stmt.setString(1, name);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                sponsorList.removeIf(sponsor -> sponsor.name.equals(name));
                JOptionPane.showMessageDialog(null, "Sponsor " + name + " removed successfully");
            } else {
                JOptionPane.showMessageDialog(null, "Sponsor " + name + " not found");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error removing sponsor: " + e.getMessage());
        }
    }

    public void removeSponsorsByEvent(String eventTitle) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM sponsors WHERE event_title = ?")) {
            stmt.setString(1, eventTitle);
            stmt.executeUpdate();
            sponsorList.removeIf(sponsor -> sponsor.eventTitle.equals(eventTitle));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error removing sponsors by event: " + e.getMessage());
        }
    }

    public double getTotalSponsorship() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT SUM(amount) AS total FROM sponsors")) {
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error calculating total sponsorship: " + e.getMessage());
        }
        return 0.0;
    }

    public void viewAllSponsors() {
        if (sponsorList.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No sponsors added yet!");
            return;
        }
        StringBuilder sb = new StringBuilder("Sponsors:\n");
        for (Sponsor sponsor : sponsorList) {
            sb.append("Name: ").append(sponsor.name)
                    .append(", Amount: $").append(sponsor.amount)
                    .append(", Event: ").append(sponsor.eventTitle).append("\n");
        }
        JOptionPane.showMessageDialog(null, sb.toString());
    }

    private void loadFromDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM sponsors")) {
            sponsorList.clear();
            while (rs.next()) {
                sponsorList.add(new Sponsor(rs.getString("name"), rs.getDouble("amount"), rs.getString("event_title")));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading sponsors: " + e.getMessage());
        }
    }
}