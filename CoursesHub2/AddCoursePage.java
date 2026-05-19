package CoursesHub2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AddCoursePage extends JFrame {
    private String selectedImagePath = "";
    private String selectedImageName = "";
    private JLabel imageLabel = new JLabel("No image selected");

    public AddCoursePage(int academyId) {
        setTitle("Add Course");
        setIconImage(new ImageIcon("src/images/bg_logo.png").getImage());
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        AnimatedGradientPanel mainPanel = new AnimatedGradientPanel();
        mainPanel.setLayout(null);

        JButton backBtn = new JButton("\u2190 Back");
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 22));
        backBtn.setBackground(new Color(255, 180, 50));
        backBtn.setForeground(Color.WHITE);
        backBtn.setBounds(30, 920, 120, 45);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> dispose());
        mainPanel.add(backBtn);

        JLabel headerImage = new JLabel(new ImageIcon(new ImageIcon("src/images/add.png").getImage().getScaledInstance(270, 270, Image.SCALE_SMOOTH)));
        headerImage.setBounds(150, 20, 270, 270);
        mainPanel.add(headerImage);

        JLabel courseImage = new JLabel(new ImageIcon(new ImageIcon("src/images/course.png").getImage().getScaledInstance(700, 700, Image.SCALE_SMOOTH)));
        courseImage.setBounds(1100, 200, 700, 700);
        mainPanel.add(courseImage);

        JPanel contentPanel = new JPanel(null);
        contentPanel.setOpaque(false);
        contentPanel.setBounds(150, 280, 1000, 900);

        int fieldWidth = 600;
        int fieldHeight = 50;
        int labelWidth = 220;
        int labelHeight = 40;
        int labelX = 0;
        int fieldX = labelX + labelWidth + 20;
        int y = 20;
        int gap = 70;

        JLabel nameLabel = new JLabel("Course Name:");
        nameLabel.setBounds(labelX, y, labelWidth, labelHeight);
        contentPanel.add(styleLabel(nameLabel));
        JTextField nameField = createGlowingField();
        nameField.setBounds(fieldX, y, fieldWidth, fieldHeight);
        contentPanel.add(nameField);

        y += gap;
        JLabel descLabel = new JLabel("Description:");
        descLabel.setBounds(labelX, y, labelWidth, labelHeight);
        contentPanel.add(styleLabel(descLabel));
        JTextArea descArea = new JTextArea();
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBorder(BorderFactory.createLineBorder(new Color(255, 180, 50), 3));
        JScrollPane scrollPane = new JScrollPane(descArea);
        scrollPane.setBounds(fieldX, y, fieldWidth, 100);
        contentPanel.add(scrollPane);

        y += 110;
        JLabel priceLabel = new JLabel("Price:");
        priceLabel.setBounds(labelX, y, labelWidth, labelHeight);
        contentPanel.add(styleLabel(priceLabel));
        JTextField priceField = createGlowingField();
        priceField.setBounds(fieldX, y, fieldWidth, fieldHeight);
        contentPanel.add(priceField);

        y += gap;
        JLabel durationLabel = new JLabel("Duration:");
        durationLabel.setBounds(labelX, y, labelWidth, labelHeight);
        contentPanel.add(styleLabel(durationLabel));
        JTextField durationField = createGlowingField();
        durationField.setBounds(fieldX, y, fieldWidth, fieldHeight);
        contentPanel.add(durationField);

        y += gap;
        JLabel startDateLabel = new JLabel("Start Date (yyyy-mm-dd):");
        startDateLabel.setBounds(labelX, y, labelWidth + 100, labelHeight);
        contentPanel.add(styleLabel(startDateLabel));
        JTextField startDateField = createGlowingField();
        startDateField.setBounds(fieldX + 100, y, fieldWidth - 100, fieldHeight);
        contentPanel.add(startDateField);

        y += gap;
        JLabel endDateLabel = new JLabel("End Date (yyyy-mm-dd):");
        endDateLabel.setBounds(labelX, y, labelWidth + 100, labelHeight);
        contentPanel.add(styleLabel(endDateLabel));
        JTextField endDateField = createGlowingField();
        endDateField.setBounds(fieldX + 100, y, fieldWidth - 100, fieldHeight);
        contentPanel.add(endDateField);

        y += gap;
        JLabel imageUploadLabel = new JLabel("Course Image:");
        imageUploadLabel.setBounds(labelX, y, labelWidth, labelHeight);
        contentPanel.add(styleLabel(imageUploadLabel));

        JButton uploadImageBtn = new JButton("Upload Image");
        uploadImageBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        uploadImageBtn.setBounds(fieldX, y, 200, 40);
        uploadImageBtn.setBackground(new Color(0, 102, 204));
        uploadImageBtn.setForeground(Color.WHITE);
        contentPanel.add(uploadImageBtn);

        imageLabel.setBounds(fieldX + 220, y, 300, 40);
        imageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        contentPanel.add(imageLabel);

        uploadImageBtn.addActionListener((ActionEvent e) -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                selectedImagePath = selectedFile.getAbsolutePath(); // للمصدر
                selectedImageName = selectedFile.getName();         // هذا اللي منخزّنه بالـDB
                imageLabel.setText(selectedImageName);
            }
        });

        JButton addBtn = new JButton("Add Course");
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 24));
        addBtn.setBounds(fieldX, y + 70, fieldWidth, 60);
        addBtn.setBackground(new Color(255, 180, 50));
        addBtn.setForeground(Color.WHITE);
        addBtn.setFocusPainted(false);
        addBtn.setBorder(BorderFactory.createLineBorder(new Color(255, 140, 30), 4));
        addBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        contentPanel.add(addBtn);

        addBtn.addActionListener(e -> addCourse(academyId, nameField, descArea, priceField, durationField, startDateField, endDateField));
        getRootPane().setDefaultButton(addBtn);

        mainPanel.add(contentPanel);
        setContentPane(mainPanel);
        setVisible(true);
    }

    private void addCourse(int academyId, JTextField nameField, JTextArea descArea, JTextField priceField,
                           JTextField durationField, JTextField startDateField, JTextField endDateField) {
        try {
            String imageNameToStore = (selectedImageName == null || selectedImageName.isEmpty())
                    ? "course.png" : selectedImageName;

            if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
                File src = new File(selectedImagePath);
                if (src.exists()) {
                    File dstDir = new File("src/images");
                    if (!dstDir.exists()) dstDir.mkdirs();

                    File dst = new File(dstDir, imageNameToStore);

                    if (!sameFile(src, dst)) {
                        String base = imageNameToStore;
                        String ext = "";
                        int dot = base.lastIndexOf('.');
                        if (dot > 0) { ext = base.substring(dot); base = base.substring(0, dot); }
                        int k = 1;
                        while (dst.exists() && !sameFile(src, dst)) {
                            dst = new File(dstDir, base + "_" + k + ext);
                            k++;
                        }
                        Files.copy(src.toPath(), dst.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        imageNameToStore = dst.getName(); 
                    } else {
                        imageNameToStore = dst.getName();
                    }
                }
            }

            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO Courses (academy_id, course_name, course_image, description, start_date, end_date, price, duration) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
            );
            stmt.setInt(1, academyId);
            stmt.setString(2, nameField.getText());
            stmt.setString(3, imageNameToStore); 
            stmt.setString(4, descArea.getText());
            stmt.setDate(5, java.sql.Date.valueOf(LocalDate.parse(startDateField.getText(), DateTimeFormatter.ISO_DATE)));
            stmt.setDate(6, java.sql.Date.valueOf(LocalDate.parse(endDateField.getText(), DateTimeFormatter.ISO_DATE)));
            stmt.setDouble(7, Double.parseDouble(priceField.getText()));
            stmt.setString(8, durationField.getText());
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Course added successfully!");
            dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private boolean sameFile(File a, File b) {
        try {
            return a.getCanonicalPath().equalsIgnoreCase(b.getCanonicalPath());
        } catch (Exception e) {
            return false;
        }
    }

    private JTextField createGlowingField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        field.setBorder(BorderFactory.createLineBorder(new Color(255, 180, 50), 3));
        return field;
    }

    private JLabel styleLabel(JLabel label) {
        label.setFont(new Font("Segoe UI", Font.BOLD, 20));
        label.setForeground(new Color(204, 102, 0));
        return label;
    }

    class AnimatedGradientPanel extends JPanel {
        private Color[] colors = {new Color(255, 250, 200), new Color(255, 230, 180)};
        private List<Circle> circles = new ArrayList<>();
        private Random rand = new Random();

        public AnimatedGradientPanel() {
            setDoubleBuffered(true);
            Timer animationTimer = new Timer(40, e -> {
                if (getWidth() > 0 && getHeight() > 0 && circles.isEmpty()) {
                    for (int i = 0; i < 50; i++) circles.add(new Circle());
                }
                for (Circle c : circles) c.update();
                repaint();
            });
            animationTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            GradientPaint gp = new GradientPaint(0, 0, colors[0], getWidth(), getHeight(), colors[1]);
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());

            for (Circle c : circles) {
                g2.setColor(new Color(255, 170, 0, 40));
                g2.fillOval((int) c.x, (int) c.y, c.size, c.size);
            }
        }

        class Circle {
            float x, y;
            int size;
            float dx, dy;

            Circle() {
                boolean rightSide = rand.nextBoolean();
                x = rightSide ? getWidth() + rand.nextInt(200) : -rand.nextInt(200);
                y = rand.nextInt(getHeight());
                size = 30 + rand.nextInt(70);
                dx = rightSide ? -1.0f * rand.nextFloat() : 1.0f * rand.nextFloat();
                dy = (rand.nextFloat() - 0.5f) * 0.5f;
            }

            void update() {
                x += dx;
                y += dy;
                if (x < -size || x > getWidth() + size || y < 0 || y > getHeight()) {
                    x = rand.nextBoolean() ? getWidth() + rand.nextInt(200) : -rand.nextInt(200);
                    y = rand.nextInt(getHeight());
                    size = 30 + rand.nextInt(70);
                }
            }
        }
    }
}
