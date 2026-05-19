package CoursesHub2;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URI;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CourseSearchPage extends JFrame {

    private JTextField tfQuery;
    private JPanel columnPanel;
    private JLabel statusLabel;
    private final Color BRAND = new Color(0, 51, 102);

    public CourseSearchPage() {
        setTitle("Find a Course");
        setIconImage(new ImageIcon("src/images/bg_logo.png").getImage());
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        GradientPanel mainPanel = new GradientPanel();
        mainPanel.setLayout(new BorderLayout());
        setContentPane(mainPanel);

        // Header
        JPanel headerWrap = new JPanel(new BorderLayout());
        headerWrap.setOpaque(false);
        headerWrap.setBorder(new EmptyBorder(28, 0, 12, 0));

        JLabel header = new JLabel("Find a Course", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 54));
        header.setForeground(BRAND);
        headerWrap.add(header, BorderLayout.NORTH);

        // Search bar  ExploreCoursesPage)
        JPanel searchBar = new JPanel(new BorderLayout(10, 0));
        searchBar.setOpaque(false);
        searchBar.setBorder(new EmptyBorder(0, 140, 10, 140));

        JLabel lbl = new JLabel("Course name:");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lbl.setForeground(BRAND);

        tfQuery = new JTextField();
        tfQuery.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        tfQuery.putClientProperty("JTextField.placeholderText",
                "e.g., AI, AI Fundamentals, Networking, French ...");

        JButton btnSearch = new JButton("Search");
        stylePrimaryButton(btnSearch);
        btnSearch.setPreferredSize(new Dimension(140, 44));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(lbl);

        searchBar.add(left, BorderLayout.WEST);
        searchBar.add(tfQuery, BorderLayout.CENTER);
        searchBar.add(btnSearch, BorderLayout.EAST);

        headerWrap.add(searchBar, BorderLayout.CENTER);
        mainPanel.add(headerWrap, BorderLayout.NORTH);

        // Results column ExploreCoursesPage)
        columnPanel = new JPanel();
        columnPanel.setLayout(new BoxLayout(columnPanel, BoxLayout.Y_AXIS));
        columnPanel.setOpaque(false);
        columnPanel.setBorder(new EmptyBorder(30, 120, 80, 120));

        JScrollPane scroll = new JScrollPane(columnPanel);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        mainPanel.add(scroll, BorderLayout.CENTER);

        // Bottom
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(8, 20, 12, 20));

        JButton backButton = new JButton("← Back");
        backButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        backButton.setForeground(BRAND);
        backButton.setBackground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backButton.setBorder(BorderFactory.createLineBorder(BRAND, 2));
        backButton.setPreferredSize(new Dimension(120, 42));
        addHover(backButton, new Color(230, 230, 230), Color.WHITE);
        backButton.addActionListener(e -> dispose());

        statusLabel = new JLabel("Type a course name then press Enter or Search.");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusLabel.setForeground(BRAND.darker());

        bottom.add(backButton, BorderLayout.WEST);
        bottom.add(statusLabel, BorderLayout.EAST);
        mainPanel.add(bottom, BorderLayout.SOUTH);

        // Actions
        ActionListener doSearch = e -> searchByName();
        btnSearch.addActionListener(doSearch);
        tfQuery.addActionListener(doSearch);

        setVisible(true);
    }

    // ===== Search =====
    private void searchByName() {
        String q = (tfQuery.getText() == null) ? "" : tfQuery.getText().trim();
        columnPanel.removeAll();

        if (q.isEmpty()) {
            statusLabel.setText("Please enter a course name to search.");
            columnPanel.revalidate();
            columnPanel.repaint();
            return;
        }

        // 1) Exact match
        List<CourseRow> exact = findCoursesExact(q);
        if (exact.size() == 1) {
            openRegistration(exact.get(0).registrationLink);
            statusLabel.setText("Opening registration for \"" + exact.get(0).courseName + "\" …");
            return;
        }
        if (!exact.isEmpty()) {
            renderCourses(exact, q + " (exact)");
            return;
        }

        // 2) LIKE search
        List<CourseRow> like = findCoursesLike(q);
        renderCourses(like, q);
    }

    private List<CourseRow> findCoursesExact(String name) {
        String sql =
            "SELECT c.course_id, c.academy_id, c.course_name, c.course_image, c.description, " +
            "       c.start_date, c.end_date, c.price, c.duration, " +
            "       a.name AS academy_name, a.registration_link " +
            "FROM Courses c JOIN Academies a ON a.academy_id = c.academy_id " +
            "WHERE c.course_name = ? " +
            "ORDER BY CASE WHEN c.start_date IS NULL THEN 1 ELSE 0 END, c.start_date ASC, c.course_name ASC";
        List<CourseRow> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (Exception e) { showDbError(e); }
        return list;
    }

    private List<CourseRow> findCoursesLike(String q) {
        String sql =
            "SELECT c.course_id, c.academy_id, c.course_name, c.course_image, c.description, " +
            "       c.start_date, c.end_date, c.price, c.duration, " +
            "       a.name AS academy_name, a.registration_link " +
            "FROM Courses c JOIN Academies a ON a.academy_id = c.academy_id " +
            "WHERE c.course_name LIKE ? " +
            "ORDER BY CASE WHEN c.start_date IS NULL THEN 1 ELSE 0 END, c.start_date ASC, c.course_name ASC";
        List<CourseRow> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + q + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (Exception e) { showDbError(e); }
        return list;
    }

    private CourseRow mapRow(ResultSet rs) throws SQLException {
        CourseRow r = new CourseRow();
        r.courseId = rs.getInt("course_id");
        r.academyId = rs.getInt("academy_id");
        r.courseName = rs.getString("course_name");
        r.courseImage = rs.getString("course_image");
        r.description = rs.getString("description");
        r.startDate = rs.getDate("start_date");
        r.endDate = rs.getDate("end_date");
        r.price = (rs.getBigDecimal("price") == null ? null : rs.getBigDecimal("price").doubleValue());
        r.duration = rs.getString("duration");
        r.academyName = rs.getString("academy_name");
        r.registrationLink = rs.getString("registration_link");
        return r;
    }

    // ===== Render ExploreCoursesPage) =====
    private void renderCourses(List<CourseRow> rows, String qLabel) {
        columnPanel.removeAll();

        if (rows == null || rows.isEmpty()) {
            statusLabel.setText("No results" + (qLabel == null ? "" : (" for \"" + qLabel + "\"")));
            columnPanel.revalidate();
            columnPanel.repaint();
            return;
        }

        statusLabel.setText(rows.size() + " result(s)" + (qLabel == null ? "" : (" for \"" + qLabel + "\"")));
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (CourseRow c : rows) {
           // ExploreCoursesPage
            JPanel card = new JPanel();
            card.setLayout(new BorderLayout(25, 25));
            card.setBackground(Color.WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 200, 240), 3),
                new EmptyBorder(30, 30, 30, 30)
            ));

            // Image 300x200
            JLabel imgLabel = new JLabel();
            ImageIcon icon = safeCourseIcon(c.courseImage);
            Image scaled = icon.getImage().getScaledInstance(300, 200, Image.SCALE_SMOOTH);
            imgLabel.setIcon(new ImageIcon(scaled));
            imgLabel.setPreferredSize(new Dimension(300, 200));
            card.add(imgLabel, BorderLayout.WEST);

            // Info
            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setBackground(Color.WHITE);

            JLabel name = new JLabel(c.courseName);
            name.setFont(new Font("Segoe UI", Font.BOLD, 32));
            name.setForeground(BRAND);

            JLabel academy = new JLabel("Academy: " + c.academyName);
            academy.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            academy.setForeground(new Color(30, 60, 120));

            String descText = c.description == null ? "" : c.description;
            JLabel desc = new JLabel("<html><p style='width:550px'>" + descText + "</p></html>");
            desc.setFont(new Font("Segoe UI", Font.PLAIN, 20));
            desc.setForeground(Color.DARK_GRAY);

            String start = c.startDate == null ? "" : c.startDate.toLocalDate().format(fmt);
            String end   = c.endDate   == null ? "" : c.endDate.toLocalDate().format(fmt);
            JLabel dates = new JLabel("Start: " + start + " | End: " + end);
            dates.setFont(new Font("Segoe UI", Font.ITALIC, 18));
            dates.setForeground(new Color(90, 90, 90));

            String price = "Price: " + (c.price == null ? "-" : String.format("$%.2f", c.price))
                         + " | Duration: " + (c.duration == null ? "-" : c.duration);
            JLabel priceLbl = new JLabel(price);
            priceLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
            priceLbl.setForeground(new Color(0, 120, 60));

            // Actions
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            actions.setBackground(Color.WHITE);

            JButton registerBtn = new JButton("Register");
            stylePrimaryButton(registerBtn);
            addHover(registerBtn, new Color(0, 40, 80), new Color(0, 51, 100));
            registerBtn.addActionListener(e -> openRegistration(c.registrationLink));
            actions.add(registerBtn);

            infoPanel.add(name);
            infoPanel.add(Box.createVerticalStrut(10));
            infoPanel.add(academy);
            infoPanel.add(Box.createVerticalStrut(10));
            infoPanel.add(desc);
            infoPanel.add(Box.createVerticalStrut(10));
            infoPanel.add(dates);
            infoPanel.add(Box.createVerticalStrut(10));
            infoPanel.add(priceLbl);
            infoPanel.add(Box.createVerticalStrut(15));
            infoPanel.add(actions);

            card.add(infoPanel, BorderLayout.CENTER);

            columnPanel.add(card);
            columnPanel.add(Box.createVerticalStrut(40)); 
        }

        columnPanel.revalidate();
        columnPanel.repaint();
    }

    // ===== Helpers =====
    private void openRegistration(String url) {
        if (url == null || url.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No registration link provided by academy.");
            return;
        }
        try {
            String finalLink = url.trim();
            if (!finalLink.toLowerCase().startsWith("http://") && !finalLink.toLowerCase().startsWith("https://")) {
                finalLink = "https://" + finalLink;
            }
            Desktop.getDesktop().browse(new URI(finalLink));
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to open browser for: " + url);
        }
    }

    private ImageIcon safeCourseIcon(String imagePath) {
        String[] candidates = new String[] {
            imagePath,
            (imagePath == null ? null :
                (imagePath.startsWith("src/images/") ? imagePath : "src/images/" + imagePath)),
            "src/images/course.png"
        };
        for (String p : candidates) {
            if (p == null) continue;
            File f = new File(p);
            if (f.exists()) return new ImageIcon(f.getPath());
        }
        return new ImageIcon("src/images/course.png");
    }

    private void stylePrimaryButton(JButton b) {
        b.setFont(new Font("Segoe UI", Font.BOLD, 20));
        b.setBackground(new Color(0, 51, 100));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(140, 44));
        b.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
    }

    private void addHover(AbstractButton btn, Color hover, Color normal) {
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(hover); }
            @Override public void mouseExited (java.awt.event.MouseEvent e) { btn.setBackground(normal); }
        });
    }

    //  ExploreCoursesPage
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
            int w = getWidth(), h = getHeight();

            GradientPaint gp = new GradientPaint(
                0, 0,  blendColors(new Color(173, 216, 230), new Color(70, 130, 180), offset),
                0, h,  blendColors(new Color(70, 130, 180), new Color(0, 51, 102), offset)
            );
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.10f));
            g2.setColor(Color.WHITE);
            int stripeHeight = 30;
            for (int i = 0; i < h + stripeHeight; i += 150) {
                g2.fillRoundRect(0, (int) (i + (offset * 100)), w, stripeHeight, 30, 30);
            }

            g2.dispose();
        }
        private Color blendColors(Color c1, Color c2, float ratio) {
            int r = (int) (c1.getRed()   * (1 - ratio) + c2.getRed()   * ratio);
            int g = (int) (c1.getGreen() * (1 - ratio) + c2.getGreen() * ratio);
            int b = (int) (c1.getBlue()  * (1 - ratio) + c2.getBlue()  * ratio);
            return new Color(r, g, b);
        }
    }

    // DTO
    private static class CourseRow {
        int courseId, academyId;
        String courseName, courseImage, description, duration, academyName, registrationLink;
        java.sql.Date startDate, endDate;
        Double price;
    }

    private void showDbError(Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CourseSearchPage::new);
    }
}
