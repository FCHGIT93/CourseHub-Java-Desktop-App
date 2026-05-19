package CoursesHub2;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ViewAcademyPage extends JFrame {

    // tweak here if you want 
    private static final int CARD_PADDING     = 18;       
    private static final int CARD_BORDER_PX   = 1;         
    private static final int CARD_IMG_W       = 820;      
    private static final int CARD_IMG_H       = 540;     
    private static final Color NAME_COLOR     = new Color(0, 51, 100);
    private int currentAcademyId;
    private String userRole;
    private JTextField searchField;
    private JPanel academyPanel;
    private List<Academy> allAcademies = new ArrayList<>();
    private JFrame parentDashboard;

    public ViewAcademyPage(String role, int academyId, JFrame parentDashboard) {
        this.userRole = role;
        this.currentAcademyId = academyId;
        this.parentDashboard = parentDashboard;

        setTitle("Academy List");
        setIconImage(new ImageIcon("src/images/bg_logo.png").getImage());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());

        // ===== Top bar =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel title = new JLabel("View Academies", JLabel.LEFT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 50));
        title.setForeground(new Color(0, 51, 90));
        topPanel.add(title, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);
        searchField = new JTextField(25);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 30));
        JButton searchBtn = new JButton("Search");
        JButton backBtn = new JButton("← Back");
        styleButton(searchBtn);
        styleButton(backBtn);

        rightPanel.add(searchField);
        rightPanel.add(searchBtn);
        rightPanel.add(backBtn);
        topPanel.add(rightPanel, BorderLayout.EAST);

        JLabel imageLabel = new JLabel();
        ImageIcon icon = new ImageIcon("src/images/search.png");
        Image img = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        imageLabel.setIcon(new ImageIcon(img));
        topPanel.add(imageLabel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        // ===== List panel =====
        academyPanel = new JPanel();
        academyPanel.setLayout(new BoxLayout(academyPanel, BoxLayout.Y_AXIS));
        academyPanel.setBackground(new Color(245, 245, 245));

        JScrollPane scrollPane = new JScrollPane(academyPanel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        // actions
        searchBtn.addActionListener(e -> {
            String keyword = searchField.getText().trim().toLowerCase();
            if (keyword.equals("all")) {
                displayAcademies(allAcademies);
            } else {
                filterAcademies(keyword);
            }
        });

        backBtn.addActionListener(e -> {
            dispose();
            if (parentDashboard != null) parentDashboard.setVisible(true);
        });

        loadAcademies();
        setVisible(true);
    }

    public void loadAcademies() {
        allAcademies.clear();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT academy_id, name, image_url FROM Academies")) {
            while (rs.next()) {
                int id = rs.getInt("academy_id");
                String name = rs.getString("name");
                String image = rs.getString("image_url");
                allAcademies.add(new Academy(id, name, image));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        displayAcademies(allAcademies);
    }

    private void filterAcademies(String keyword) {
        List<Academy> filtered = new ArrayList<>();
        for (Academy a : allAcademies) {
            if (a.getName().toLowerCase().contains(keyword)) filtered.add(a);
        }
        displayAcademies(filtered);
    }

    private void displayAcademies(List<Academy> list) {
        academyPanel.removeAll();
        for (Academy academy : list) {
            academyPanel.add(createAcademyCard(academy));
            academyPanel.add(Box.createRigidArea(new Dimension(0, 16)));
        }
        academyPanel.revalidate();
        academyPanel.repaint();
    }

    private JPanel createAcademyCard(Academy academy) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), CARD_BORDER_PX),
                BorderFactory.createEmptyBorder(CARD_PADDING, CARD_PADDING, CARD_PADDING, CARD_PADDING)
        ));

        String imgPath = "src/images/" + academy.getImageUrl();
        ImageIcon icon = new ImageIcon(imgPath);
        Image scaled = icon.getImage().getScaledInstance(CARD_IMG_W, CARD_IMG_H, Image.SCALE_SMOOTH);
        JLabel imgLabel = new JLabel(new ImageIcon(scaled));
        imgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameLabel = new JLabel(academy.getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        nameLabel.setForeground(NAME_COLOR);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton exploreBtn = new JButton("Explore");
        exploreBtn.setBackground(new Color(0, 51, 102));
        exploreBtn.setForeground(Color.WHITE);
        exploreBtn.setFont(new Font("Segoe UI", Font.BOLD, 24));
        exploreBtn.setFocusPainted(false);
        exploreBtn.addActionListener(e -> new ExploreCoursesPage(academy.getId()));

        JButton addCourseBtn = new JButton("Add Course");
        addCourseBtn.setBackground(new Color(0, 165, 255));
        addCourseBtn.setForeground(Color.WHITE);
        addCourseBtn.setFont(new Font("Segoe UI", Font.BOLD, 22));
        addCourseBtn.setFocusPainted(false);

        JButton editCourseBtn = new JButton("Edit Course");
        editCourseBtn.setBackground(new Color(255, 140, 0)); // ORANGE
        editCourseBtn.setForeground(Color.WHITE);
        editCourseBtn.setFont(new Font("Segoe UI", Font.BOLD, 22));
        editCourseBtn.setFocusPainted(false);

        JButton deleteCourseBtn = new JButton("Delete Course");
        deleteCourseBtn.setBackground(new Color(204, 0, 0));
        deleteCourseBtn.setForeground(Color.WHITE);
        deleteCourseBtn.setFont(new Font("Segoe UI", Font.BOLD, 22));
        deleteCourseBtn.setFocusPainted(false);

        boolean isAdmin = userRole.equalsIgnoreCase("admin");
        boolean isOwner = userRole.equalsIgnoreCase("academy") && currentAcademyId == academy.getId();

        addCourseBtn.setVisible(isAdmin || isOwner);
        editCourseBtn.setVisible(isAdmin || isOwner);
        deleteCourseBtn.setVisible(isAdmin || isOwner);

        addCourseBtn.addActionListener(e -> new AddCoursePage(academy.getId()));
        editCourseBtn.addActionListener(e -> new EditCourseDialog(ViewAcademyPage.this, userRole, academy.getId()).setVisible(true));
        deleteCourseBtn.addActionListener(e -> new DeleteCoursePage(academy.getId()));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 0)); // hgap = 18px
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(exploreBtn);
        btnPanel.add(addCourseBtn);
        btnPanel.add(editCourseBtn);
        btnPanel.add(deleteCourseBtn);

        card.add(imgLabel);
        card.add(Box.createRigidArea(new Dimension(0, 18)));
        card.add(nameLabel);
        card.add(Box.createRigidArea(new Dimension(0, 18)));
        card.add(btnPanel);

        card.setMaximumSize(new Dimension(CARD_IMG_W + CARD_PADDING * 2 + 6,
                                          CARD_IMG_H + 160));
        return card;
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setBackground(new Color(0, 51, 102));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}
