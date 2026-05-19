package CoursesHub2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URI;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExploreCoursesPage extends JFrame {

    public ExploreCoursesPage(int academyId) {
        setTitle("Explore Courses");
        setIconImage(new ImageIcon("src/images/bg_logo.png").getImage());
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        GradientPanel mainPanel = new GradientPanel();
        mainPanel.setLayout(new BorderLayout());

        // Top header
        JLabel header = new JLabel("Explore Our Courses", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 60));
        header.setForeground(new Color(0, 51, 102));
        header.setBorder(BorderFactory.createEmptyBorder(40, 0, 40, 0));
        mainPanel.add(header, BorderLayout.NORTH);

        // Column panel for courses
        JPanel columnPanel = new JPanel();
        columnPanel.setLayout(new BoxLayout(columnPanel, BoxLayout.Y_AXIS));
        columnPanel.setOpaque(false);
        columnPanel.setBorder(BorderFactory.createEmptyBorder(30, 120, 80, 120));

        List<Course> courses = getCoursesByAcademy(academyId);

        for (Course course : courses) {
            JPanel card = new JPanel();
            card.setLayout(new BorderLayout(25, 25));
            card.setBackground(Color.WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(180, 200, 240), 3),
                    BorderFactory.createEmptyBorder(30, 30, 30, 30)
            ));

            // Course Image (safe fallback)
            JLabel imgLabel = new JLabel();
            ImageIcon icon = safeCourseIcon(course.getImagePath());
            Image scaled = icon.getImage().getScaledInstance(300, 200, Image.SCALE_SMOOTH);
            imgLabel.setIcon(new ImageIcon(scaled));
            imgLabel.setPreferredSize(new Dimension(300, 200));
            card.add(imgLabel, BorderLayout.WEST);

            // Course Info
            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setBackground(Color.WHITE);

            JLabel name = new JLabel(course.getName());
            name.setFont(new Font("Segoe UI", Font.BOLD, 32));
            name.setForeground(new Color(0, 51, 102));

            String descText = (course.getDescription() == null) ? "" : course.getDescription();
            JLabel desc = new JLabel("<html><p style='width:550px'>" + descText + "</p></html>");
            desc.setFont(new Font("Segoe UI", Font.PLAIN, 20));
            desc.setForeground(Color.DARK_GRAY);

            String start = (course.getStartDate() == null) ? "" : course.getStartDate().toString();
            String end   = (course.getEndDate()   == null) ? "" : course.getEndDate().toString();
            JLabel dates = new JLabel("Start: " + start + " | End: " + end);
            dates.setFont(new Font("Segoe UI", Font.ITALIC, 18));
            dates.setForeground(new Color(90, 90, 90));

            JLabel price = new JLabel("Price: $" + course.getPrice() + " | Duration: " + (course.getDuration() == null ? "" : course.getDuration()));
            price.setFont(new Font("Segoe UI", Font.BOLD, 18));
            price.setForeground(new Color(0, 120, 60));

            // Actions (Register)
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            actions.setBackground(Color.WHITE);

            JButton registerBtn = new JButton("Register");
            stylePrimaryButton(registerBtn);
            addHover(registerBtn, new Color(0, 40, 80), new Color(0, 51, 100));
            registerBtn.addActionListener(e -> openRegistrationForAcademy(course.getacademyId(), card));

            // Enter key triggers Register (Java 8-safe: WHEN_IN_FOCUSED_WINDOW)
            registerBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                    .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "goRegister");
            registerBtn.getActionMap().put("goRegister", new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) {
                    if (registerBtn.isFocusOwner()) {
                        openRegistrationForAcademy(course.getacademyId(), card);
                    }
                }
            });

            actions.add(registerBtn);

            infoPanel.add(name);
            infoPanel.add(Box.createVerticalStrut(10));
            infoPanel.add(desc);
            infoPanel.add(Box.createVerticalStrut(10));
            infoPanel.add(dates);
            infoPanel.add(Box.createVerticalStrut(10));
            infoPanel.add(price);
            infoPanel.add(Box.createVerticalStrut(15));
            infoPanel.add(actions);

            card.add(infoPanel, BorderLayout.CENTER);
            columnPanel.add(card);
            columnPanel.add(Box.createVerticalStrut(40));
        }

        JScrollPane scroll = new JScrollPane(columnPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        mainPanel.add(scroll, BorderLayout.CENTER);

        // Back Button (Bottom-Left)
        JButton backButton = new JButton("← Back");
        backButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        backButton.setForeground(new Color(0, 51, 102));
        backButton.setBackground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backButton.setBorder(BorderFactory.createLineBorder(new Color(0, 51, 102), 2));
        backButton.setPreferredSize(new Dimension(120, 42));
        backButton.addActionListener(e -> dispose());
        addHover(backButton, new Color(230, 230, 230), Color.WHITE);

        JPanel backPanel = new JPanel(new BorderLayout());
        backPanel.setOpaque(false);
        backPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));
        backPanel.add(backButton, BorderLayout.WEST);
        mainPanel.add(backPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        setVisible(true);
    }

    // open registration link by academy_id (Java 8-safe)
    private void openRegistrationForAcademy(int academyId, Component parent) {
        String link = null;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT registration_link FROM Academies WHERE academy_id = ?")) {
            ps.setInt(1, academyId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) link = rs.getString(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(parent, "❌ Database error while fetching registration link.");
            return;
        }

        if (link == null || link.trim().isEmpty()) { // isEmpty بدل isBlank
            JOptionPane.showMessageDialog(parent, "⚠ No registration link set for this academy.");
            return;
        }

        try {
            String finalLink = link.trim();
            if (!finalLink.toLowerCase().startsWith("http://") && !finalLink.toLowerCase().startsWith("https://")) {
                finalLink = "https://" + finalLink;
            }
            Desktop.getDesktop().browse(new URI(finalLink));
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, "❌ Failed to open browser for: " + link);
        }
    }

    private List<Course> getCoursesByAcademy(int academyId) {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT * FROM Courses WHERE academy_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, academyId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new Course(
                        rs.getInt("course_id"),
                        rs.getInt("academy_id"),
                        rs.getString("course_name"),
                        rs.getString("course_image"),
                        rs.getString("description"),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        rs.getDouble("price"),
                        rs.getString("duration")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // Safe icon loader (fallback to placeholder if missing)
    private ImageIcon safeCourseIcon(String imagePath) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            return new ImageIcon("src/images/course.png");
        }
        File f = new File("src/images/" + imagePath);
        if (!f.exists()) return new ImageIcon("src/images/course.png");
        return new ImageIcon(f.getPath());
    }

    private void stylePrimaryButton(JButton b) {
        b.setFont(new Font("Segoe UI", Font.BOLD, 20));
        b.setBackground(new Color(0, 51, 100));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(180, 50));
        b.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
    }

    private void addHover(AbstractButton btn, Color hover, Color normal) {
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(hover); }
            @Override public void mouseExited (java.awt.event.MouseEvent e) { btn.setBackground(normal); }
        });
    }

    // Background
    class GradientPanel extends JPanel {
        private float offset = 0f;

        public GradientPanel() {
            Timer timer = new Timer(50, e -> {
                offset += 0.01f;
                if (offset > 1f) offset = 0f;
                repaint();
            });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();

            int w = getWidth();
            int h = getHeight();

            GradientPaint gp = new GradientPaint(
                    0, 0, blendColors(new Color(173, 216, 230), new Color(70, 130, 180), offset),
                    0, h, blendColors(new Color(70, 130, 180), new Color(0, 51, 102), offset)
            );
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
            g2.setColor(Color.WHITE);
            int stripeHeight = 30;
            for (int i = 0; i < h + stripeHeight; i += 150) {
                g2.fillRoundRect(0, (int) (i + (offset * 100)), w, stripeHeight, 30, 30);
            }

            g2.dispose();
        }

        private Color blendColors(Color c1, Color c2, float ratio) {
            int r = (int) (c1.getRed() * (1 - ratio) + c2.getRed() * ratio);
            int g = (int) (c1.getGreen() * (1 - ratio) + c2.getGreen() * ratio);
            int b = (int) (c1.getBlue() * (1 - ratio) + c2.getBlue() * ratio);
            return new Color(r, g, b);
        }
    }
}
