
package CoursesHub2;



import java.sql.Date;

public class Course {
    private int id, academyId;
    private String name, imagePath, description, duration;
    private Date startDate, endDate;
    private double price;

    public Course(int id, int academyId, String name, String imagePath, String description,
                  Date startDate, Date endDate, double price, String duration) {
        this.id = id;
        this.academyId = academyId;
        this.name = name;
        this.imagePath = imagePath;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.price = price;
        this.duration = duration;
    }
    public int getacademyId(){return academyId;};
    public String getName() { return name; }
    public String getImagePath() { return imagePath; }
    public String getDescription() { return description; }
    public Date getStartDate() { return startDate; }
    public Date getEndDate() { return endDate; }
    public double getPrice() { return price; }
    public String getDuration() { return duration; }
}

