import user.Users;
import events.Events;
import sponsors.Sponsors;
import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {
        Events events = new Events();
        Users users = new Users(events);
        Sponsors sponsors = new Sponsors(events);

        JFrame frame = new JFrame("Event Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(450, 300);
        frame.setLayout(new GridLayout(5, 1));

        JButton userButton = new JButton("User Operations");
        JButton eventButton = new JButton("Event Operations");
        JButton judgeButton = new JButton("Judge Operations");
        JButton sponsorButton = new JButton("Sponsor Operations");
        JButton exitButton = new JButton("Exit");

        userButton.addActionListener(e -> users.userOperations());
        eventButton.addActionListener(e -> events.eventOperations(users, sponsors));
        judgeButton.addActionListener(e -> events.judgeOperations());
        sponsorButton.addActionListener(e -> sponsors.sponsorOperations());
        exitButton.addActionListener(e -> System.exit(0));

        frame.add(userButton);
        frame.add(eventButton);
        frame.add(judgeButton);
        frame.add(sponsorButton);
        frame.add(exitButton);

        frame.setVisible(true);
    }
}