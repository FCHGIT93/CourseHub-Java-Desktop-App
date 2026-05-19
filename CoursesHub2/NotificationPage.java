package CoursesHub2;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*; 
import java.sql.*;
import java.text.SimpleDateFormat;

public class NotificationPage extends JFrame {
    private String userRole;
    private int academyId;

    public NotificationPage(int academyId, String userRole) {
        this.userRole = userRole;
        this.academyId = academyId;

        setTitle("Notifications");
        setIconImage(new ImageIcon("src/images/bg_logo.png").getImage());
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setIconImage(new ImageIcon("src/images/bg_logo.png").getImage());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(255, 247, 214));
        mainPanel.setBorder(new EmptyBorder(40, 80, 40, 80));

        JLabel header = new JLabel("Notifications", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 50));
        header.setForeground(new Color(0, 51, 90));
        header.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(header);
        mainPanel.add(Box.createVerticalStrut(30));

        Color unreadBtn = new Color(255, 140, 0);
        Color readBtn   = new Color(0, 51, 102);  

        try (Connection conn = DBConnection.getConnection()) {
            String sql =
                "SELECT N.notif_id, N.message, N.is_read, " +
                "       J.domain, " +
                "       A.firstname, A.lastname, A.photo_path, A.app_id, " +
                "       N.created_at " +
                "FROM Notifications N " +
                "JOIN JobApplications A ON N.app_id = A.app_id " +
                "JOIN Jobs J ON N.job_id = J.job_id " +
                (userRole.equals("academy") ? "WHERE J.academy_id = ? " : "") +
                "ORDER BY N.created_at DESC";

            PreparedStatement ps = conn.prepareStatement(sql);
            if (userRole.equals("academy")) ps.setInt(1, academyId);

            ResultSet rs = ps.executeQuery();
            boolean hasNotif = false;
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            while (rs.next()) {
                hasNotif = true;
                int notifId = rs.getInt("notif_id");
                String message = rs.getString("message");
                boolean isRead = rs.getBoolean("is_read");
                String domain = rs.getString("domain");
                String firstname = rs.getString("firstname");
                String lastname = rs.getString("lastname");
                String photoPath = rs.getString("photo_path");
                int appId = rs.getInt("app_id");
                Timestamp createdAt = rs.getTimestamp("created_at");

                JPanel notifCard = new JPanel(new BorderLayout());
                notifCard.setBorder(BorderFactory.createLineBorder(new Color(0, 51, 90), 2));
                notifCard.setBackground(Color.WHITE);
                notifCard.setMaximumSize(new Dimension(1200, 200));

                
                JPanel leftPanel = new JPanel(new BorderLayout());
                leftPanel.setBackground(Color.WHITE);
                JLabel imgLabel = new JLabel();
                imgLabel.setPreferredSize(new Dimension(160, 160));
                if (photoPath != null && !photoPath.isEmpty()) {
                    ImageIcon icon = new ImageIcon(photoPath);
                    Image scaled = icon.getImage().getScaledInstance(160, 160, Image.SCALE_SMOOTH);
                    imgLabel.setIcon(new ImageIcon(scaled));
                } else {
                    imgLabel.setText("No Image");
                    imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
                }
                leftPanel.add(imgLabel, BorderLayout.CENTER);
                JPanel centerPanel = new JPanel();
                centerPanel.setBackground(Color.WHITE);
                centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

                JLabel msgLabel = new JLabel(message);
                msgLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
                msgLabel.setForeground(new Color(0, 102, 0));

                JPanel msgWithIconPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                msgWithIconPanel.setBackground(Color.WHITE);
                JLabel iconLabel = new JLabel();
                ImageIcon alertIcon = new ImageIcon("src/images/sah.png");
                Image scaledIcon = alertIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
                iconLabel.setIcon(new ImageIcon(scaledIcon));

                msgWithIconPanel.add(iconLabel);
                msgWithIconPanel.add(Box.createHorizontalStrut(10));
                msgWithIconPanel.add(msgLabel);
                msgWithIconPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

                JLabel nameLabel = new JLabel("Candidate: " + firstname + " " + lastname);
                nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 20));
                nameLabel.setForeground(new Color(0, 51, 90));

                JLabel domainLabel = new JLabel("Domain: " + domain);
                domainLabel.setFont(new Font("Segoe UI", Font.PLAIN, 20));
                domainLabel.setForeground(new Color(0, 51, 90));

                centerPanel.add(msgWithIconPanel);
                centerPanel.add(Box.createVerticalStrut(10));
                centerPanel.add(nameLabel);
                centerPanel.add(domainLabel);

                // RIGHT: 
                JPanel eastPanel = new JPanel();
                eastPanel.setLayout(new BoxLayout(eastPanel, BoxLayout.Y_AXIS));
                eastPanel.setBackground(Color.WHITE);

                JLabel timeLabel = new JLabel(formatter.format(createdAt));
                timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
                timeLabel.setForeground(Color.GRAY);

                JButton viewBtn = new JButton("View Applicant");
                viewBtn.setBackground(isRead ? readBtn : unreadBtn); 
                viewBtn.setForeground(Color.WHITE);
                viewBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
                viewBtn.setFocusPainted(false);
                viewBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                viewBtn.addActionListener(e -> {
                    markAsRead(notifId);
                    viewBtn.setBackground(readBtn);
                    AdminDashboard dash = AdminDashboard.getInstance();
                    if (dash != null) SwingUtilities.invokeLater(dash::refreshNotificationsBadge);

                    this.setVisible(false);
                    new ViewApplicantPage(appId, this);
                });

                eastPanel.add(timeLabel);
                eastPanel.add(Box.createVerticalStrut(10));
                eastPanel.add(viewBtn);

                notifCard.add(leftPanel, BorderLayout.WEST);
                notifCard.add(centerPanel, BorderLayout.CENTER);
                notifCard.add(eastPanel, BorderLayout.EAST);

                mainPanel.add(notifCard);
                mainPanel.add(Box.createVerticalStrut(20));
            }

            if (!hasNotif) {
                JLabel emptyLabel = new JLabel(" No notifications found.");
                emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 28));
                emptyLabel.setForeground(Color.GRAY);
                emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                mainPanel.add(emptyLabel);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "❌ Error loading notifications: " + e.getMessage());
        }

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setBackground(new Color(255, 247, 214));

        JButton backBtn = new JButton("Back");
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 20));
        backBtn.setBackground(new Color(0, 51, 90));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFocusPainted(false);
        backBtn.setPreferredSize(new Dimension(140, 45));
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> {
            dispose();
            AdminDashboard dash = AdminDashboard.getInstance();
            if (dash != null) SwingUtilities.invokeLater(dash::refreshNotificationsBadge);
            AdminDashboard.getInstance().setVisible(true);
        });

        bottomPanel.add(backBtn);

        JPanel container = new JPanel(new BorderLayout());
        container.add(scrollPane, BorderLayout.CENTER);
        container.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(container);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) {
                AdminDashboard dash = AdminDashboard.getInstance();
                if (dash != null) SwingUtilities.invokeLater(dash::refreshNotificationsBadge);
            }
        });

        setVisible(true);
    }

    private void markAsRead(int notifId) {
        try (Connection conn = DBConnection.getConnection()) {
            String updateSql = "UPDATE Notifications SET is_read = 1 WHERE notif_id = ?";
            PreparedStatement ps = conn.prepareStatement(updateSql);
            ps.setInt(1, notifId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
