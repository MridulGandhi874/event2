package events;

import user.Users;
import sponsors.Sponsors;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;

public class Events {

    private ArrayList<Event> eventList;
    private ArrayList<Judge> judgeList;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/event_management";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "your_password";

    private class Event {
        String title;
        String date;
        String time;
        String venue;

        Event(String title, String date, String time, String venue) {
            this.title = title;
            this.date = date;
            this.time = time;
            this.venue = venue;
        }
    }

    public Events() {
        eventList = new ArrayList<>();
        judgeList = new ArrayList<>();
        loadEventsFromDatabase();
        loadJudgesFromDatabase();
    }

    public void eventOperations(Users users, Sponsors sponsors) {
        JFrame frame = new JFrame("Event Operations");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new GridLayout(4, 1));

        JButton addButton = new JButton("Add Event");
        JButton removeButton = new JButton("Remove Event");
        JButton viewButton = new JButton("View All Events");
        JButton backButton = new JButton("Back to Main Menu");

        addButton.addActionListener(e -> showAddEventDialog());
        removeButton.addActionListener(e -> showRemoveEventDialog(users, sponsors));
        viewButton.addActionListener(e -> viewAllEvents());
        backButton.addActionListener(e -> frame.dispose());

        frame.add(addButton);
        frame.add(removeButton);
        frame.add(viewButton);
        frame.add(backButton);

        frame.setVisible(true);
    }

    public void judgeOperations() {
        JFrame frame = new JFrame("Judge Operations");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new GridLayout(4, 1));

        JButton addButton = new JButton("Add Judge");
        JButton removeButton = new JButton("Remove Judge");
        JButton viewButton = new JButton("View All Judges");
        JButton backButton = new JButton("Back to Main Menu");

        addButton.addActionListener(e -> showAddJudgeDialog());
        removeButton.addActionListener(e -> showRemoveJudgeDialog());
        viewButton.addActionListener(e -> viewAllJudges());
        backButton.addActionListener(e -> frame.dispose());

        frame.add(addButton);
        frame.add(removeButton);
        frame.add(viewButton);
        frame.add(backButton);

        frame.setVisible(true);
    }

    private void showAddEventDialog() {
        JDialog dialog = new JDialog((Frame) null, "Add Event", true);
        dialog.setSize(300, 200);
        dialog.setLayout(new GridLayout(5, 2));

        JTextField titleField = new JTextField();
        JTextField dateField = new JTextField();
        JTextField timeField = new JTextField();
        JTextField venueField = new JTextField();

        dialog.add(new JLabel("Event Title:"));
        dialog.add(titleField);
        dialog.add(new JLabel("Date (DD-MM-YYYY):"));
        dialog.add(dateField);
        dialog.add(new JLabel("Time (HH:MM):"));
        dialog.add(timeField);
        dialog.add(new JLabel("Venue:"));
        dialog.add(venueField);

        JButton submitButton = new JButton("Add");
        submitButton.addActionListener(e -> {
            try {
                String title = titleField.getText();
                String date = dateField.getText();
                String time = timeField.getText();
                String venue = venueField.getText();
                addEvent(title, date, time, venue);
                dialog.dispose();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage());
            }
        });

        dialog.add(submitButton);
        dialog.setVisible(true);
    }

    private void showRemoveEventDialog(Users users, Sponsors sponsors) {
        JDialog dialog = new JDialog((Frame) null, "Remove Event", true);
        dialog.setSize(450, 300);
        dialog.setLayout(new GridLayout(2, 2));

        JTextField titleField = new JTextField();
        dialog.add(new JLabel("Event Title:"));
        dialog.add(titleField);

        JButton submitButton = new JButton("Remove");
        submitButton.addActionListener(e -> {
            removeEvent(titleField.getText(), users, sponsors);
            dialog.dispose();
        });

        dialog.add(submitButton);
        dialog.setVisible(true);
    }

    private void showAddJudgeDialog() {
        JDialog dialog = new JDialog((Frame) null, "Add Judge", true);
        dialog.setSize(450, 300);
        dialog.setLayout(new GridLayout(3, 2));

        JTextField nameField = new JTextField();
        JTextField eventTitleField = new JTextField();

        dialog.add(new JLabel("Judge Name:"));
        dialog.add(nameField);
        dialog.add(new JLabel("Event Title:"));
        dialog.add(eventTitleField);

        JButton submitButton = new JButton("Add");
        submitButton.addActionListener(e -> {
            String name = nameField.getText();
            String eventTitle = eventTitleField.getText();
            if (eventExists(eventTitle)) {
                addJudge(name, eventTitle);
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Event does not exist!");
            }
        });

        dialog.add(submitButton);
        dialog.setVisible(true);
    }

    private void showRemoveJudgeDialog() {
        JDialog dialog = new JDialog((Frame) null, "Remove Judge", true);
        dialog.setSize(450, 300);
        dialog.setLayout(new GridLayout(2, 2));

        JTextField nameField = new JTextField();
        dialog.add(new JLabel("Judge Name:"));
        dialog.add(nameField);

        JButton submitButton = new JButton("Remove");
        submitButton.addActionListener(e -> {
            removeJudge(nameField.getText());
            dialog.dispose();
        });

        dialog.add(submitButton);
        dialog.setVisible(true);
    }

    public void addEvent(String title, String date, String time, String venue) throws IllegalArgumentException {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Event title cannot be empty");
        }
        if (!date.matches("\\d{2}-\\d{2}-\\d{4}")) {
            throw new IllegalArgumentException("Date must be in DD-MM-YYYY format");
        }
        if (!time.matches("\\d{2}:\\d{2}")) {
            throw new IllegalArgumentException("Time must be in HH:MM format");
        }
        if (venue == null || venue.trim().isEmpty()) {
            throw new IllegalArgumentException("Venue cannot be empty");
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO events (title, date, time, venue) VALUES (?, ?, ?, ?)")) {
            stmt.setString(1, title);
            stmt.setString(2, date);
            stmt.setString(3, time);
            stmt.setString(4, venue);
            stmt.executeUpdate();
            eventList.add(new Event(title, date, time, venue));
            JOptionPane.showMessageDialog(null, "Event " + title + " added successfully");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error adding event: " + e.getMessage());
        }
    }

    public void addJudge(String name, String eventTitle) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO judges (name, event_title) VALUES (?, ?)")) {
            stmt.setString(1, name);
            stmt.setString(2, eventTitle);
            stmt.executeUpdate();
            judgeList.add(new Judge(name, eventTitle));
            JOptionPane.showMessageDialog(null, "Judge " + name + " added successfully for " + eventTitle);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error adding judge: " + e.getMessage());
        }
    }

    public void removeEvent(String title, Users users, Sponsors sponsors) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM events WHERE title = ?")) {
            stmt.setString(1, title);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                eventList.removeIf(event -> event.title.equals(title));
                judgeList.removeIf(judge -> judge.getEventTitle().equals(title));
                users.removeUsersByEvent(title);
                sponsors.removeSponsorsByEvent(title);
                JOptionPane.showMessageDialog(null, "Event " + title + " removed successfully");
            } else {
                JOptionPane.showMessageDialog(null, "Event " + title + " not found");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error removing event: " + e.getMessage());
        }
    }

    public void removeJudge(String name) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM judges WHERE name = ?")) {
            stmt.setString(1, name);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                judgeList.removeIf(judge -> judge.getName().equals(name));
                JOptionPane.showMessageDialog(null, "Judge " + name + " removed successfully");
            } else {
                JOptionPane.showMessageDialog(null, "Judge " + name + " not found");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error removing judge: " + e.getMessage());
        }
    }

    public boolean eventExists(String title) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM events WHERE title = ?")) {
            stmt.setString(1, title);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error checking event: " + e.getMessage());
            return false;
        }
    }

    public void viewAllEvents() {
        if (eventList.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No events scheduled yet!");
            return;
        }
        StringBuilder sb = new StringBuilder("Scheduled Events:\n");
        for (Event event : eventList) {
            sb.append("Title: ").append(event.title)
                    .append(", Date: ").append(event.date)
                    .append(", Time: ").append(event.time)
                    .append(", Venue: ").append(event.venue).append("\n");
        }
        JOptionPane.showMessageDialog(null, sb.toString());
    }

    public void viewAllJudges() {
        if (judgeList.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No judges assigned yet!");
            return;
        }
        StringBuilder sb = new StringBuilder("Assigned Judges:\n");
        for (Judge judge : judgeList) {
            sb.append("Name: ").append(judge.getName())
                    .append(", Event: ").append(judge.getEventTitle()).append("\n");
        }
        JOptionPane.showMessageDialog(null, sb.toString());
    }

    private void loadEventsFromDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM events")) {
            eventList.clear();
            while (rs.next()) {
                eventList.add(new Event(rs.getString("title"), rs.getString("date"), rs.getString("time"), rs.getString("venue")));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading events: " + e.getMessage());
        }
    }

    private void loadJudgesFromDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM judges")) {
            judgeList.clear();
            while (rs.next()) {
                judgeList.add(new Judge(rs.getString("name"), rs.getString("event_title")));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading judges: " + e.getMessage());
        }
    }
}