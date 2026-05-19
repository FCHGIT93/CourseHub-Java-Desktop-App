
package CoursesHub2;


import java.sql.*;

public final class NotificationService {

    private NotificationService() {}

    public static int createNotificationsForNewApplication(int appId) {
        String fetchApp = "SELECT academy_id, domain, experience_years, skills " +
                          "FROM JobApplications WHERE app_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement psApp = conn.prepareStatement(fetchApp)) {

            psApp.setInt(1, appId);
            try (ResultSet rs = psApp.executeQuery()) {
                if (!rs.next()) return 0;

                int academyId = rs.getInt("academy_id");
                String domain = rs.getString("domain");
                Integer expYears = (Integer) rs.getObject("experience_years");

                String insertNotif =
                    "INSERT INTO Notifications(app_id, job_id, message, is_read) " +
                    "SELECT ?, J.job_id, ?, 0 " +
                    "FROM Jobs J " +
                    "WHERE J.academy_id = ? " +
                    "  AND ((J.domain IS NULL AND ? IS NULL) OR J.domain = ?) " +
                    "  AND (J.min_experience IS NULL OR J.min_experience <= ?) " +
                    "  AND NOT EXISTS ( " +
                    "      SELECT 1 FROM Notifications N WHERE N.app_id = ? AND N.job_id = J.job_id " +
                    "  )";

                try (PreparedStatement ps = conn.prepareStatement(insertNotif)) {
                    int i = 1;
                    ps.setInt(i++, appId);
                    ps.setString(i++, "New applicant matches your job requirements");
                    ps.setInt(i++, academyId);
                    ps.setString(i++, domain);
                    ps.setString(i++, domain);
                    if (expYears == null) ps.setNull(i++, Types.INTEGER); else ps.setInt(i++, expYears);
                    ps.setInt(i++, appId);
                    return ps.executeUpdate();
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public static int createNotificationsForNewJob(int jobId) {
        String fetchJob = "SELECT academy_id, domain, min_experience, required_skills " +
                          "FROM Jobs WHERE job_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement psJob = conn.prepareStatement(fetchJob)) {

            psJob.setInt(1, jobId);
            try (ResultSet rs = psJob.executeQuery()) {
                if (!rs.next()) return 0;

                int academyId = rs.getInt("academy_id");
                String domain = rs.getString("domain");
                Integer minExp = (Integer) rs.getObject("min_experience");

                String insertNotif =
                    "INSERT INTO Notifications(app_id, job_id, message, is_read) " +
                    "SELECT A.app_id, ?, ?, 0 " +
                    "FROM JobApplications A " +
                    "WHERE A.academy_id = ? " +
                    "  AND ((A.domain IS NULL AND ? IS NULL) OR A.domain = ?) " +
                    "  AND (? IS NULL OR A.experience_years IS NULL OR A.experience_years >= ?) " +
                    "  AND NOT EXISTS ( " +
                    "      SELECT 1 FROM Notifications N WHERE N.app_id = A.app_id AND N.job_id = ? " +
                    "  )";

                try (PreparedStatement ps = conn.prepareStatement(insertNotif)) {
                    int i = 1;
                    ps.setInt(i++, jobId);
                    ps.setString(i++, "A new job matches one or more applicants");
                    ps.setInt(i++, academyId);
                    ps.setString(i++, domain);
                    ps.setString(i++, domain);
                    if (minExp == null) {
                        ps.setNull(i++, Types.INTEGER);
                        ps.setNull(i++, Types.INTEGER);
                    } else {
                        ps.setInt(i++, minExp);
                        ps.setInt(i++, minExp);
                    }
                    ps.setInt(i++, jobId);
                    return ps.executeUpdate();
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public static int getUnreadCountForAcademy(int academyId) {
        String sql = "SELECT COUNT(*) " +
                     "FROM Notifications N " +
                     "JOIN Jobs J ON N.job_id = J.job_id " +
                     "WHERE J.academy_id = ? AND N.is_read = 0";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, academyId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public static int markAllAsReadForAcademy(int academyId) {
        String sql = "UPDATE N SET N.is_read = 1 " +
                     "FROM Notifications N " +
                     "JOIN Jobs J ON N.job_id = J.job_id " +
                     "WHERE J.academy_id = ? AND N.is_read = 0";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, academyId);
            return ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
}
