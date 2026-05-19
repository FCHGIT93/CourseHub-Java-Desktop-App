package CoursesHub2;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.util.ArrayList;
import CoursesHub2.NotificationService; 

public class AddJobPage extends JFrame {
    private JTextField domainField, experienceField, ageField, skillsField, salaryField;
    private JComboBox<String> academyComboBox;
    private final String userRole;
    private int academyId;
    private String academyName;
    private final ArrayList<Integer> academyIds = new ArrayList<>();

    public AddJobPage(String userRole, int academyId) {
        this.userRole = userRole;
        this.academyId = academyId;
        this.academyName = fetchAcademyName(academyId);

        setTitle("Post a Job Opportunity");
        setIconImage(new ImageIcon("src/images/bg_logo.png").getImage());
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setIconImage(new ImageIcon("src/images/bg_logo.png").getImage());
        getContentPane().setBackground(new Color(255, 247, 214));
        setLayout(new BorderLayout());

        Color headerColor = new Color(0, 51, 102);
        Color buttonColor = new Color(255, 140, 0);

        JLabel titleLabel = new JLabel("Post a Job Opportunity", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setForeground(headerColor);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
        add(titleLabel, BorderLayout.NORTH);

        JLabel imageLabel = new JLabel();
        ImageIcon originalIcon = new ImageIcon("src/images/addjob.png");
        Image scaledImage = originalIcon.getImage().getScaledInstance(800, 800, Image.SCALE_SMOOTH);
        imageLabel.setIcon(new ImageIcon(scaledImage));

        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBackground(new Color(255, 247, 214));
        imagePanel.add(imageLabel, BorderLayout.CENTER);
        add(imagePanel, BorderLayout.WEST);

        JPanel formPanel = new JPanel(new GridLayout(userRole.equals("admin") ? 7 : 6, 2, 15, 15));
        formPanel.setBorder(BorderFactory.createEmptyBorder(40, 100, 40, 100));
        formPanel.setBackground(new Color(255, 247, 214));

        JLabel domainLabel = new JLabel("Job Domain:");
        JLabel experienceLabel = new JLabel("Min Experience (Years):");
        JLabel ageLabel = new JLabel("Min Age:");
        JLabel skillsLabel = new JLabel("Required Skills:");
        JLabel salaryLabel = new JLabel("Salary ($):");

        JLabel[] labels = {domainLabel, experienceLabel, ageLabel, skillsLabel, salaryLabel};
        for (JLabel lbl : labels) {
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 30));
            lbl.setForeground(headerColor);
        }

        domainField = new JTextField();
        experienceField = new JTextField();
        ageField = new JTextField();
        skillsField = new JTextField();
        salaryField = new JTextField();

        JTextField[] fields = {domainField, experienceField, ageField, skillsField, salaryField};
        for (JTextField field : fields) {
            field.setFont(new Font("Segoe UI", Font.PLAIN, 30));
            field.setBorder(new LineBorder(headerColor, 2));
            field.setForeground(headerColor);
        }

        if (userRole.equals("admin")) {
            JLabel comboLabel = new JLabel("Academy:");
            comboLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
            comboLabel.setForeground(headerColor);

            academyComboBox = new JComboBox<>();
            academyComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 20));
            academyComboBox.setBackground(Color.WHITE);
            academyComboBox.setForeground(headerColor);
            academyComboBox.setBorder(new LineBorder(headerColor, 2));
            loadAcademyOptions();

            formPanel.add(comboLabel);
            formPanel.add(academyComboBox);
        } else {
            JLabel fixedAcademy = new JLabel("Academy: " + academyName);
            fixedAcademy.setFont(new Font("Segoe UI", Font.BOLD, 22));
            fixedAcademy.setForeground(headerColor);
            formPanel.add(fixedAcademy);
            formPanel.add(new JLabel(""));
        }

        formPanel.add(domainLabel);     formPanel.add(domainField);
        formPanel.add(experienceLabel); formPanel.add(experienceField);
        formPanel.add(ageLabel);        formPanel.add(ageField);
        formPanel.add(skillsLabel);     formPanel.add(skillsField);
        formPanel.add(salaryLabel);     formPanel.add(salaryField);

        JButton backBtn = new JButton("Back");
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 30));
        backBtn.setBackground(headerColor);
        backBtn.setForeground(Color.WHITE);
        backBtn.setFocusPainted(false);
        backBtn.setPreferredSize(new Dimension(100, 40));
        backBtn.addActionListener(e -> this.dispose());

        JButton submitBtn = new JButton("Submit Job");
        submitBtn.setFont(new Font("Segoe UI", Font.BOLD, 30));
        submitBtn.setBackground(buttonColor);
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFocusPainted(false);
        submitBtn.setPreferredSize(new Dimension(300, 80));
        submitBtn.addActionListener(this::submitJob);

        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setBackground(new Color(255, 247, 214));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 20));
        leftPanel.setBackground(new Color(255, 247, 214));
        leftPanel.add(backBtn);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 20));
        rightPanel.setBackground(new Color(255, 247, 214));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 30, 10));
        rightPanel.add(submitBtn);

        buttonPanel.add(leftPanel, BorderLayout.WEST);
        buttonPanel.add(rightPanel, BorderLayout.EAST);

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setBackground(new Color(255, 247, 214));
        centerWrapper.add(formPanel, BorderLayout.CENTER);
        centerWrapper.add(buttonPanel, BorderLayout.SOUTH);

        add(centerWrapper, BorderLayout.CENTER);
        setVisible(true);
    }

    private void submitJob(ActionEvent e) {
        String domain = domainField.getText().trim();
        String experience = experienceField.getText().trim();
        String age = ageField.getText().trim();
        String skills = skillsField.getText().trim();
        String salary = salaryField.getText().trim().replace("$", "").replace(",", "");

        if (domain.isEmpty() || experience.isEmpty() || age.isEmpty() || skills.isEmpty() || salary.isEmpty()) {
            JOptionPane.showMessageDialog(this, "⚠️ Please fill in all fields.");
            return;
        }

        if (userRole.equals("admin") && academyComboBox != null && academyComboBox.getSelectedIndex() >= 0) {
            academyId = academyIds.get(academyComboBox.getSelectedIndex());
        }

        try {
            int expVal = Integer.parseInt(experience);
            int ageVal = Integer.parseInt(age);
            java.math.BigDecimal salaryVal = new java.math.BigDecimal(salary);

            try (Connection conn = DBConnection.getConnection()) {
                String sql = "INSERT INTO Jobs (academy_id, domain, min_experience, min_age, required_skills, salary) " +
                             "VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, academyId);
                ps.setString(2, domain);
                ps.setInt(3, expVal);
                ps.setInt(4, ageVal);
                ps.setString(5, skills);
                ps.setBigDecimal(6, salaryVal);

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    int jobId = -1;
                    try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            jobId = generatedKeys.getInt(1);
                        }
                    }

                    int notified = 0;
                    if (jobId > 0) {
                        notified = NotificationService.createNotificationsForNewJob(jobId);
                        AdminDashboard dash = AdminDashboard.getInstance();
                        if (dash != null) {
                            SwingUtilities.invokeLater(dash::refreshNotificationsBadge);
                        }
                    }

                    JOptionPane.showMessageDialog(this,
                        "✅ Job posted successfully!\n🔔 Notifications sent to " + notified + " applicant(s).");
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "⚠️ Failed to post job.");
                }
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "⚠️ Please enter valid numbers for experience, age, and salary.");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "❌ Database error: " + ex.getMessage());
        }
    }

    private void loadAcademyOptions() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT academy_id, name FROM Academies");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                academyIds.add(rs.getInt("academy_id"));
                academyComboBox.addItem(rs.getString("name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String fetchAcademyName(int academyId) {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT name FROM Academies WHERE academy_id = ?");
            ps.setInt(1, academyId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("name");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown Academy";
    }
}

