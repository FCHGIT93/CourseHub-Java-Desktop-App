package CoursesHub2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DeleteCoursePage extends JFrame {
    public DeleteCoursePage(int academyId) {
        setTitle("Delete Course");
        setIconImage(new ImageIcon("src/images/bg_logo.png").getImage());
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        AnimatedBackgroundPanel mainPanel = new AnimatedBackgroundPanel();
        mainPanel.setLayout(null);

        JLabel title = new JLabel("Delete Course");
        title.setFont(new Font("Segoe UI", Font.BOLD, 50));
        title.setForeground(Color.WHITE);
        title.setBounds(100, 50, 500, 60);
        mainPanel.add(title);

        try {
            ImageIcon delIcon = new ImageIcon("src/images/delete.png");
            Image scaled = delIcon.getImage().getScaledInstance(700, 700, Image.SCALE_SMOOTH);
            JLabel imgRight = new JLabel(new ImageIcon(scaled));
            imgRight.setBounds(1220, 170, 700, 700); 
            mainPanel.add(imgRight);
        } catch (Exception ignore) {
            
        }

        int y = 150;
        int gap = 150;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT course_id, course_name FROM Courses WHERE academy_id = ?")) {

            stmt.setInt(1, academyId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("course_id");
                String name = rs.getString("course_name");

                JPanel courseCard = new JPanel(null);
                courseCard.setBackground(Color.WHITE);
                courseCard.setBounds(100, y, 1000, 100);
                courseCard.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));

                JLabel courseLabel = new JLabel(name);
                courseLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
                courseLabel.setForeground(new Color(0, 51, 90));
                courseLabel.setBounds(30, 25, 600, 40);
                courseCard.add(courseLabel);

                JButton deleteBtn = new JButton("Delete");
                deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 22));
                deleteBtn.setBounds(820, 25, 130, 40);
                deleteBtn.setBackground(new Color(204, 0, 0));
                deleteBtn.setForeground(Color.WHITE);
                deleteBtn.setFocusPainted(false);
                deleteBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                deleteBtn.addActionListener(e -> deleteCourse(id));
                courseCard.add(deleteBtn);

                mainPanel.add(courseCard);
                y += gap;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Back
        JButton backBtn = new JButton("← Back");
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 22));
        backBtn.setBounds(30, 900, 120, 45);
        backBtn.setBackground(new Color(0, 102, 204));
        backBtn.setForeground(Color.WHITE);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener((ActionEvent e) -> dispose());
        mainPanel.add(backBtn);

        setContentPane(mainPanel);
        setVisible(true);
    }

    private void deleteCourse(int courseId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM Courses WHERE course_id = ?")) {
            stmt.setInt(1, courseId);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Course deleted successfully!");
            dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    class AnimatedBackgroundPanel extends JPanel {
        private List<Blob> blobs = new ArrayList<>();
        private Random rand = new Random();

        public AnimatedBackgroundPanel() {
            setDoubleBuffered(true);
            Timer timer = new Timer(30, e -> {
                if (blobs.isEmpty() && getWidth() > 0 && getHeight() > 0) {
                    for (int i = 0; i < 15; i++) blobs.add(new Blob());
                }
                for (Blob b : blobs) b.update();
                repaint();
            });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(170, 210, 255),
                    getWidth(), getHeight(), new Color(200, 230, 255));
            g2.setPaint(gradient);
            g2.fillRect(0, 0, getWidth(), getHeight());

            for (Blob b : blobs) {
                g2.setColor(new Color(255, 255, 255, 40));
                g2.fillRoundRect((int) b.x, (int) b.y, b.size, b.size, b.size / 2, b.size / 2);
            }
        }

        class Blob {
            float x, y, dx, dy;
            int size;

            Blob() {
                size = 60 + rand.nextInt(80);
                x = rand.nextInt(1300);
                y = rand.nextInt(900);
                dx = (rand.nextFloat() - 0.5f) * 2;
                dy = (rand.nextFloat() - 0.5f) * 2;
            }

            void update() {
                x += dx;
                y += dy;
                if (x < -size || x > getWidth())  x = rand.nextInt(Math.max(1, getWidth()));
                if (y < -size || y > getHeight()) y = rand.nextInt(Math.max(1, getHeight()));
            }
        }
    }
}
