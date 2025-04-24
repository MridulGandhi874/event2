package events;

public class Judge {

    private String name;
    private String eventTitle;

    public Judge(String name, String eventTitle) throws IllegalArgumentException {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Judge name cannot be empty");
        }
        if (eventTitle == null || eventTitle.trim().isEmpty()) {
            throw new IllegalArgumentException("Event title cannot be empty");
        }
        this.name = name;
        this.eventTitle = eventTitle;
    }

    public String getName() {
        return name;
    }

    public String getEventTitle() {
        return eventTitle;
    }
}