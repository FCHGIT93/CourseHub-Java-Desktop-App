
package CoursesHub2;

import java.sql.Timestamp;

public class Job {
    private int jobId;
    private int academyId;
    private String academyName;
    private String domain;
    private int minExperience;
    private int minAge;
    private String requiredSkills;
    private double salary;
    private Timestamp createdAt;

    public Job(int jobId, int academyId, String academyName, String domain, int minExperience,
               int minAge, String requiredSkills, double salary, Timestamp createdAt) {
        this.jobId = jobId;
        this.academyId = academyId;
        this.academyName = academyName;
        this.domain = domain;
        this.minExperience = minExperience;
        this.minAge = minAge;
        this.requiredSkills = requiredSkills;
        this.salary = salary;
        this.createdAt = createdAt;
    }

    // Getters 
    public int getJobId() { return jobId; }
    public int getAcademyId() { return academyId; }
    public String getAcademyName() { return academyName; }
    public String getDomain() { return domain; }
    public int getMinExperience() { return minExperience; }
    public int getMinAge() { return minAge; }
    public String getRequiredSkills() { return requiredSkills; }
    public double getSalary() { return salary; }
    public Timestamp getCreatedAt() { return createdAt; }

    // Setters  ViewJobsPage) 
    public void setAcademyId(int academyId) { this.academyId = academyId; }
    public void setAcademyName(String academyName) { this.academyName = academyName; }
    public void setDomain(String domain) { this.domain = domain; }
    public void setMinExperience(int minExperience) { this.minExperience = minExperience; }
    public void setMinAge(int minAge) { this.minAge = minAge; }
    public void setRequiredSkills(String requiredSkills) { this.requiredSkills = requiredSkills; }
    public void setSalary(double salary) { this.salary = salary; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Job{" +
                "jobId=" + jobId +
                ", academyId=" + academyId +
                ", academyName='" + academyName + '\'' +
                ", domain='" + domain + '\'' +
                ", minExperience=" + minExperience +
                ", minAge=" + minAge +
                ", requiredSkills='" + requiredSkills + '\'' +
                ", salary=" + salary +
                ", createdAt=" + createdAt +
                '}';
    }
}

