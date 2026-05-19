package CoursesHub2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.*;
import java.util.HashMap;
import com.toedter.calendar.JDateChooser;
import javax.sound.sampled.*;
import CoursesHub2.NotificationService; 

public class ApplyForJobPage extends JFrame {
    private JTextField firstNameField, lastNameField, phoneField, emailField;
    private JTextField experienceField, domainField;
    private JTextArea skillsArea;
    private JComboBox<String> academyComboBox;
    private JButton browseCVBtn, browsePhotoBtn, submitBtn;
    private JLabel cvSelectedLabel, photoSelectedLabel;
    private String cvPath = "";
    private String photoPath = "";
    private HashMap<String, Integer> academyMap = new HashMap<>();
    private JDateChooser birthdateChooser;

    public ApplyForJobPage() {
        setTitle("Apply for Job");
        setIconImage(new ImageIcon("src/images/bg_logo.png").getImage());
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        Color bgColor = new Color(255, 247, 214);
        Color textColor = new Color(0, 0, 100);
        Color mainBlue = new Color(0, 51, 102);

        Font labelFont = new Font("Segoe UI", Font.BOLD, 20);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 18);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(bgColor);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(bgColor);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(bgColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Job Application Form", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        titleLabel.setForeground(textColor);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        formPanel.add(titleLabel, gbc);
        gbc.gridwidth = 1;
        gbc.gridy++;

        // First Name
        addLabelAndField(formPanel, gbc, "First Name:", labelFont, textColor);
        firstNameField = createTextField(fieldFont, textColor);
        gbc.gridx = 1; formPanel.add(firstNameField, gbc);
        gbc.gridx = 0; gbc.gridy++;

        // Last Name
        addLabelAndField(formPanel, gbc, "Last Name:", labelFont, textColor);
        lastNameField = createTextField(fieldFont, textColor);
        gbc.gridx = 1; formPanel.add(lastNameField, gbc);
        gbc.gridx = 0; gbc.gridy++;

        // Birthdate
        addLabelAndField(formPanel, gbc, "Birthdate:", labelFont, textColor);
        birthdateChooser = new JDateChooser();
        birthdateChooser.setDateFormatString("yyyy-MM-dd");
        gbc.gridx = 1; formPanel.add(birthdateChooser, gbc);
        gbc.gridx = 0; gbc.gridy++;

        // Phone
        addLabelAndField(formPanel, gbc, "Phone:", labelFont, textColor);
        phoneField = createTextField(fieldFont, textColor);
        gbc.gridx = 1; formPanel.add(phoneField, gbc);
        gbc.gridx = 0; gbc.gridy++;

        // Email
        addLabelAndField(formPanel, gbc, "Email:", labelFont, textColor);
        emailField = createTextField(fieldFont, textColor);
        gbc.gridx = 1; formPanel.add(emailField, gbc);
        gbc.gridx = 0; gbc.gridy++;

        // Domain
        addLabelAndField(formPanel, gbc, "Domain:", labelFont, textColor);
        domainField = createTextField(fieldFont, textColor);
        gbc.gridx = 1; formPanel.add(domainField, gbc);
        gbc.gridx = 0; gbc.gridy++;

        // Experience
        addLabelAndField(formPanel, gbc, "Experience Years:", labelFont, textColor);
        experienceField = createTextField(fieldFont, textColor);
        gbc.gridx = 1; formPanel.add(experienceField, gbc);
        gbc.gridx = 0; gbc.gridy++;

        // Skills
        JLabel skillsLabel = new JLabel("Skills:", JLabel.RIGHT);
        skillsLabel.setFont(labelFont); skillsLabel.setForeground(textColor);
        formPanel.add(skillsLabel, gbc);
        skillsArea = new JTextArea(4, 20);
        skillsArea.setFont(fieldFont); skillsArea.setForeground(textColor);
        skillsArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        gbc.gridx = 1; formPanel.add(new JScrollPane(skillsArea), gbc);
        gbc.gridx = 0; gbc.gridy++;

        // Academy
        JLabel academyLabel = new JLabel("Select Academy:", JLabel.RIGHT);
        academyLabel.setFont(labelFont); academyLabel.setForeground(textColor);
        formPanel.add(academyLabel, gbc);
        academyComboBox = new JComboBox<>();
        academyComboBox.setFont(fieldFont);
        gbc.gridx = 1; formPanel.add(academyComboBox, gbc);
        loadAcademiesFromDB();

        // Upload Photo
        gbc.gridx = 0; gbc.gridy++;
        JLabel photoLabel = new JLabel("Upload Photo:", JLabel.RIGHT);
        photoLabel.setFont(labelFont); photoLabel.setForeground(textColor);
        formPanel.add(photoLabel, gbc);
        JPanel photoPanel = new JPanel(new BorderLayout()); photoPanel.setBackground(bgColor);
        browsePhotoBtn = new JButton("Browse Image"); browsePhotoBtn.setBackground(mainBlue); browsePhotoBtn.setForeground(Color.WHITE);
        photoSelectedLabel = new JLabel("No image selected"); photoSelectedLabel.setFont(new Font("Segoe UI", Font.ITALIC, 15));
        photoPanel.add(browsePhotoBtn, BorderLayout.WEST); photoPanel.add(photoSelectedLabel, BorderLayout.CENTER);
        gbc.gridx = 1; formPanel.add(photoPanel, gbc);
        browsePhotoBtn.addActionListener(e -> choosePhotoFile());

        // Upload CV
        gbc.gridx = 0; gbc.gridy++;
        JLabel cvLabel = new JLabel("Upload CV:", JLabel.RIGHT);
        cvLabel.setFont(labelFont); cvLabel.setForeground(textColor);
        formPanel.add(cvLabel, gbc);
        JPanel cvPanel = new JPanel(new BorderLayout()); cvPanel.setBackground(bgColor);
        browseCVBtn = new JButton("Browse PDF"); browseCVBtn.setBackground(mainBlue); browseCVBtn.setForeground(Color.WHITE);
        cvSelectedLabel = new JLabel("No file selected"); cvSelectedLabel.setFont(new Font("Segoe UI", Font.ITALIC, 15));
        cvPanel.add(browseCVBtn, BorderLayout.WEST); cvPanel.add(cvSelectedLabel, BorderLayout.CENTER);
        gbc.gridx = 1; formPanel.add(cvPanel, gbc);
        browseCVBtn.addActionListener(e -> chooseCVFile());

        // Submit Button
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        submitBtn = new JButton("Submit Application");
        submitBtn.setFont(new Font("Segoe UI", Font.BOLD, 20));
        submitBtn.setBackground(mainBlue);
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setPreferredSize(new Dimension(250, 45));
        formPanel.add(submitBtn, gbc);
        submitBtn.addActionListener(e -> submitApplication());

        // Back Button
        gbc.gridy++;
        JButton backBtn = new JButton("Back");
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 20));
        backBtn.setBackground(new Color(0, 51, 102));
        backBtn.setForeground(Color.WHITE);
        backBtn.setPreferredSize(new Dimension(180, 45));
        backBtn.setFocusPainted(false);
        backBtn.addActionListener(e -> {
            dispose();
            AdminDashboard.getInstance().setVisible(true);
        });
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setBackground(bgColor);
        bottomPanel.add(backBtn);

        contentPanel.add(formPanel, BorderLayout.CENTER);
        contentPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Right Image
        ImageIcon jobIcon = new ImageIcon("src/images/job.png");
        Image jobImage = jobIcon.getImage().getScaledInstance(800, 800, Image.SCALE_SMOOTH);
        JLabel jobImageLabel = new JLabel(new ImageIcon(jobImage));
        jobImageLabel.setHorizontalAlignment(JLabel.CENTER);
        contentPanel.add(jobImageLabel, BorderLayout.EAST);

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        setContentPane(mainPanel);
        setVisible(true);
    }

    private void addLabelAndField(JPanel panel, GridBagConstraints gbc, String text, Font font, Color color) {
        JLabel label = new JLabel(text, JLabel.RIGHT);
        label.setFont(font); label.setForeground(color);
        panel.add(label, gbc);
    }

    private JTextField createTextField(Font font, Color color) {
        JTextField tf = new JTextField();
        tf.setFont(font); tf.setForeground(color);
        tf.setPreferredSize(new Dimension(300, 35));
        return tf;
    }

    private void loadAcademiesFromDB() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT academy_id, name FROM Academies")) {
            while (rs.next()) {
                int id = rs.getInt("academy_id");
                String name = rs.getString("name");
                academyComboBox.addItem(name);
                academyMap.put(name, id);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading academies!");
        }
    }

    private void choosePhotoFile() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Image Files", "jpg", "jpeg", "png"));
        int result = fc.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            photoPath = file.getAbsolutePath();
            photoSelectedLabel.setText("Selected: " + file.getName());
        }
    }

    private void chooseCVFile() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Files", "pdf"));
        int result = fc.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            cvPath = file.getAbsolutePath();
            cvSelectedLabel.setText("Selected: " + file.getName());
        }
    }

    private void submitApplication() {
        String first = firstNameField.getText().trim();
        String last = lastNameField.getText().trim();
        java.util.Date selectedDate = birthdateChooser.getDate();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String domain = domainField.getText().trim();
        String expStr = experienceField.getText().trim();
        String skills = skillsArea.getText().trim();
        String selectedAcademy = (String) academyComboBox.getSelectedItem();

        if (first.isEmpty() || last.isEmpty() || selectedDate == null ||
            phone.isEmpty() || email.isEmpty() || domain.isEmpty() ||
            expStr.isEmpty() || skills.isEmpty() || cvPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "⚠ Please fill all fields and upload your CV!");
            return;
        }

        int experienceYears;
        try {
            experienceYears = Integer.parseInt(expStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Experience years must be a number!");
            return;
        }

        Integer academyId = academyMap.get(selectedAcademy);
        if (academyId == null) {
            JOptionPane.showMessageDialog(this, "Please select a valid academy.");
            return;
        }

        String sql = "INSERT INTO JobApplications " +
                     "(firstname, lastname, birthdate, phone, email, cv_path, photo_path, experience_years, academy_id, domain, skills) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            int i = 1;
            ps.setString(i++, first);
            ps.setString(i++, last);
            ps.setDate(i++, new java.sql.Date(selectedDate.getTime()));
            ps.setString(i++, phone);
            ps.setString(i++, email);
            ps.setString(i++, cvPath);
            if (photoPath == null || photoPath.isEmpty()) ps.setNull(i++, Types.VARCHAR); else ps.setString(i++, photoPath);
            ps.setInt(i++, experienceYears);
            ps.setInt(i++, academyId);
            ps.setString(i++, domain);
            ps.setString(i++, skills);

            int rows = ps.executeUpdate();
            if (rows <= 0) {
                JOptionPane.showMessageDialog(this, "❌ Failed to submit application!");
                return;
            }

            // get new app_id
            int newAppId = -1;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) newAppId = keys.getInt(1);
            }

            if (newAppId > 0) {
                // 1) create notifications
                int created = NotificationService.createNotificationsForNewApplication(newAppId);

                // 2) refresh dashboard badge (red number + sound if increased)
                AdminDashboard dash = AdminDashboard.getInstance();
                if (dash != null) {
                    SwingUtilities.invokeLater(dash::refreshNotificationsBadge);
                }

                // 3) UX
                showAnimatedMessage();
            } else {
                JOptionPane.showMessageDialog(this, "Saved but failed to retrieve application ID.");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
        }
    }

    private void playSuccessSound() {
        try {
            File soundFile = new File("src/sounds/success.wav");
            if (!soundFile.exists()) return;
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAnimatedMessage() {
        JDialog dialog = new JDialog(this, "Submitted!", true);
        dialog.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        dialog.setLocationRelativeTo(this);
        Color bgColor = new Color(255, 247, 214);
        Color textColor = new Color(0, 0, 100);

        JPanel panel = new JPanel(null);
        panel.setBackground(bgColor);

        JLabel message = new JLabel("Application Submitted Successfully!", JLabel.CENTER);
        message.setFont(new Font("Segoe UI", Font.BOLD, 36));
        message.setForeground(textColor);
        message.setBounds(500, 500, 900, 60);

        ImageIcon successIcon = new ImageIcon("src/images/good.png");
        Image scaledImage = successIcon.getImage().getScaledInstance(400, 400, Image.SCALE_SMOOTH);
        JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
        imageLabel.setBounds(750, 100, 400, 400);

        JButton backBtn = new JButton("Back to Main");
        backBtn.setBounds(800, 600, 250, 50);
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 24));
        backBtn.setBackground(new Color(0, 51, 102));
        backBtn.setForeground(Color.WHITE);
        backBtn.addActionListener(ev -> {
            dialog.dispose();
            dispose();
            AdminDashboard.getInstance().setVisible(true);
        });

        panel.add(message);
        panel.add(imageLabel);
        panel.add(backBtn);
        dialog.add(panel);

        playSuccessSound();
        dialog.setVisible(true);
    }
}
