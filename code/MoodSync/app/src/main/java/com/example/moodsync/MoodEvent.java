import java.util.Date;
import java.util.UUID;

public class MoodEvent {
    private UUID id;
    private String mood;
    private Date date;
    private String description;
    private String location;
    private String socialSituation;
    private String photoPath;

    // Default constructor
    public MoodEvent() {
        this.id = UUID.randomUUID();
        this.date = new Date();
    }

    // Parameterized constructor
    public MoodEvent(String mood, String description, String location, String socialSituation, String photoPath) {
        this();  // Call default constructor to set id and date
        this.mood = mood;
        this.description = description;
        this.location = location;
        this.socialSituation = socialSituation;
        this.photoPath = photoPath;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getMood() {
        return mood;
    }

    public Date getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public String getSocialSituation() {
        return socialSituation;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    // Setters
    public void setMood(String mood) {
        this.mood = mood;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setSocialSituation(String socialSituation) {
        this.socialSituation = socialSituation;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    // Additional methods
    public boolean hasPhoto() {
        return photoPath != null && !photoPath.isEmpty();
    }

    public boolean hasLocation() {
        return location != null && !location.isEmpty();
    }

    @Override
    public String toString() {
        return "MoodEvent{" +
                "id=" + id +
                ", mood='" + mood + '\'' +
                ", date=" + date +
                ", description='" + description + '\'' +
                ", location='" + location + '\'' +
                ", socialSituation='" + socialSituation + '\'' +
                ", hasPhoto=" + hasPhoto() +
                '}';
    }
}
