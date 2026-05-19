package CoursesHub2;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DeleteAcademyPage extends JFrame {

    private JComboBox<String> academyComboBox;
    private List<Integer> academyIds = new ArrayList<>();
    private JFrame parentPage;

    public DeleteAcademyPage(JFrame parentPage) {
        this.parentPage = parentPage;

        setTitle("Delete Academy");
        setIconImage(new ImageIcon("src/images/bg_logo.png").getImage());
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JLabel backgroundLabel = new JLabel(new ImageIcon("src/images/background_gradient_fullscreen.png"));
        backgroundLabel.setLayout(new BorderLayout());

        JPanel container = new JPanel(new GridBagLayout());
        container.setOpaque(false); // Transparent to show background
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Color mainBlue = new Color(0, 51, 102);
        Color textColor = new Color(100, 0, 0);
        Font font = new Font("Segoe UI", Font.BOLD, 20);

        JLabel title = new JLabel("Delete Academy", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 40));
        title.setForeground(textColor);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        container.add(title, gbc);
        gbc.gridwidth = 1;

        JLabel selectLabel = new JLabel("Select Academy:");
        selectLabel.setFont(font);
        selectLabel.setForeground(textColor);
        gbc.gridy++;
        gbc.gridx = 0;
        container.add(selectLabel, gbc);

        academyComboBox = new JComboBox<>();
        academyComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        academyComboBox.setPreferredSize(new Dimension(400, 40));
        gbc.gridx = 1;
        container.add(academyComboBox, gbc);

        loadAcademies();

        JButton deleteBtn = new JButton("Delete");
        deleteBtn.setBackground(new Color(153, 0, 0));
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 22));
        deleteBtn.setPreferredSize(new Dimension(200, 50));
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        container.add(deleteBtn, gbc);

        JButton backBtn = new JButton("\u2190 Back");
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        backBtn.setBackground(mainBlue);
        backBtn.setForeground(Color.WHITE);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        container.add(backBtn, gbc);

        deleteBtn.addActionListener(e -> deleteSelectedAcademy());
        backBtn.addActionListener(e -> {
            dispose();
            if (parentPage != null) parentPage.setVisible(true);
        });

        backgroundLabel.add(container, BorderLayout.CENTER);
        setContentPane(backgroundLabel);
        setVisible(true);
    }

    private void loadAcademies() {
        academyComboBox.removeAllItems();
        academyIds.clear();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT academy_id, name FROM Academies")) {

            while (rs.next()) {
                int id = rs.getInt("academy_id");
                String name = rs.getString("name");
                academyComboBox.addItem(name);
                academyIds.add(id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading academies.");
        }
    }

    private void deleteSelectedAcademy() {
        int selectedIndex = academyComboBox.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "No academy selected.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this, "Are you sure you want to delete this academy?", "Confirm",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        int academyId = academyIds.get(selectedIndex);

        try (Connection conn = DBConnection.getConnection()) {

            String deleteUsers = "DELETE FROM Users WHERE academy_id = ?";
            try (PreparedStatement psUsers = conn.prepareStatement(deleteUsers)) {
                psUsers.setInt(1, academyId);
                psUsers.executeUpdate();
            }

            String deleteAcademy = "DELETE FROM Academies WHERE academy_id = ?";
            try (PreparedStatement psAcademy = conn.prepareStatement(deleteAcademy)) {
                psAcademy.setInt(1, academyId);
                int rows = psAcademy.executeUpdate();
                if (rows > 0) {
                    JOptionPane.showMessageDialog(this, "Academy deleted successfully.");

                    if (parentPage instanceof ViewAcademyPage) {
                        ((ViewAcademyPage) parentPage).loadAcademies();
                        parentPage.setVisible(true);
                    }
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete academy.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error while deleting.");
        }
    }
}
