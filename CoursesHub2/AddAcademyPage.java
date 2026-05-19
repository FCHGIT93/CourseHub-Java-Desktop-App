// hover effect + enter key binding + consistent label color + fixed back button
package CoursesHub2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.security.MessageDigest;
import java.sql.*;

public class AddAcademyPage extends JFrame {
    private JTextField nameField, linkField, usernameField;
    private JPasswordField passwordField;
    private JLabel imagePathLabel;
    private JComboBox<String> roleCombo;
    private String imagePath = "";
    private ViewAcademyPage parentPage;

    public AddAcademyPage(ViewAcademyPage parent) {
        this.parentPage = parent;

        setTitle("Add New Academy");
        setIconImage(new ImageIcon("src/images/bg_logo.png").getImage());
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JLabel backgroundLabel = new JLabel(new ImageIcon("src/images/background_gradient_fullscreen.png"));
        backgroundLabel.setLayout(null);

        JLabel girlLabel = new JLabel(new ImageIcon("src/images/girl_right_side.png"));
        girlLabel.setBounds(1250, 100, 650, 850);
        backgroundLabel.add(girlLabel);

        Font labelFont = new Font("Segoe UI", Font.BOLD, 30);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 26);
        Color labelColor = new Color(0, 51, 100);

        int startX = 150;
        int startY = 220;
        int labelWidth = 270;
        int fieldWidth = 550;
        int height = 50;
        int gap = 70;

        JLabel titleLabel = new JLabel("Add New Academy");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 46));
        titleLabel.setForeground(labelColor);
        titleLabel.setBounds(startX, 100, 600, 60);
        backgroundLabel.add(titleLabel);

        JLabel nameLabel = new JLabel("Academy Name:");
        nameLabel.setBounds(startX, startY, labelWidth, height);
        nameLabel.setFont(labelFont);
        nameLabel.setForeground(labelColor);
        backgroundLabel.add(nameLabel);

        nameField = new JTextField();
        nameField.setBounds(startX + labelWidth + 20, startY, fieldWidth, height);
        nameField.setFont(fieldFont);
        backgroundLabel.add(nameField);

        JLabel linkLabel = new JLabel("Registration Link:");
        linkLabel.setBounds(startX, startY + gap, labelWidth, height);
        linkLabel.setFont(labelFont);
        linkLabel.setForeground(labelColor);
        backgroundLabel.add(linkLabel);

        linkField = new JTextField();
        linkField.setBounds(startX + labelWidth + 20, startY + gap, fieldWidth, height);
        linkField.setFont(fieldFont);
        backgroundLabel.add(linkField);

        JLabel imageLabel = new JLabel("Upload Image:");
        imageLabel.setBounds(startX, startY + gap * 2, labelWidth, height);
        imageLabel.setFont(labelFont);
        imageLabel.setForeground(labelColor);
        backgroundLabel.add(imageLabel);

        JButton uploadBtn = new JButton("Choose Image");
        uploadBtn.setBounds(startX + labelWidth + 20, startY + gap * 2, 200, height);
        uploadBtn.setFont(fieldFont);
        uploadBtn.setBackground(new Color(0, 51, 100));
        uploadBtn.setForeground(Color.WHITE);
        backgroundLabel.add(uploadBtn);

        imagePathLabel = new JLabel("No file selected");
        imagePathLabel.setBounds(startX + labelWidth + 240, startY + gap * 2, 300, height);
        imagePathLabel.setFont(fieldFont);
        imagePathLabel.setForeground(labelColor);
        backgroundLabel.add(imagePathLabel);

        uploadBtn.addActionListener(e -> chooseImageFile());

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(startX, startY + gap * 3, labelWidth, height);
        usernameLabel.setFont(labelFont);
        usernameLabel.setForeground(labelColor);
        backgroundLabel.add(usernameLabel);

        usernameField = new JTextField();
        usernameField.setBounds(startX + labelWidth + 20, startY + gap * 3, fieldWidth, height);
        usernameField.setFont(fieldFont);
        backgroundLabel.add(usernameField);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(startX, startY + gap * 4, labelWidth, height);
        passwordLabel.setFont(labelFont);
        passwordLabel.setForeground(labelColor);
        backgroundLabel.add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(startX + labelWidth + 20, startY + gap * 4, fieldWidth, height);
        passwordField.setFont(fieldFont);
        backgroundLabel.add(passwordField);

        JLabel roleLabel = new JLabel("Role:");
        roleLabel.setBounds(startX, startY + gap * 5, labelWidth, height);
        roleLabel.setFont(labelFont);
        roleLabel.setForeground(labelColor);
        backgroundLabel.add(roleLabel);

        roleCombo = new JComboBox<>(new String[]{"academy", "admin"});
        roleCombo.setBounds(startX + labelWidth + 20, startY + gap * 5, fieldWidth, height);
        roleCombo.setFont(fieldFont);
        backgroundLabel.add(roleCombo);

        JButton addBtn = new JButton("Add Academy");
        addBtn.setBounds(startX + 150, startY + gap * 6, 320, 55);
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 30));
        addBtn.setBackground(new Color(0, 51, 100));
        addBtn.setForeground(Color.WHITE);
        backgroundLabel.add(addBtn);
        addBtn.addActionListener(e -> insertAcademyAndUser());

        addBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                addBtn.setBackground(new Color(0, 40, 80));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                addBtn.setBackground(new Color(0, 51, 100));
            }
        });

        getRootPane().setDefaultButton(addBtn);

        JButton backBtn = new JButton("Back");
        backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        backBtn.setBackground(new Color(0, 51, 100));
        backBtn.setForeground(Color.WHITE);
        backBtn.setBounds(30, Toolkit.getDefaultToolkit().getScreenSize().height - 140, 140, 45);
        backgroundLabel.add(backBtn);
        backBtn.addActionListener(e -> {
            dispose();
            if (parentPage != null) parentPage.setVisible(true);
        });

        backBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                backBtn.setBackground(new Color(0, 40, 80));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                backBtn.setBackground(new Color(0, 51, 100));
            }
        });

        setContentPane(backgroundLabel);
        setVisible(true);
    }

    private void chooseImageFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Images", "jpg", "jpeg", "png"));
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selected = fileChooser.getSelectedFile();
            imagePath = selected.getName();
            imagePathLabel.setText(imagePath);
        }
    }
    private void insertAcademyAndUser() {
        String name = nameField.getText().trim();
        String link = linkField.getText().trim();
        String image = imagePath.trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String role = roleCombo.getSelectedItem().toString();

        if (name.isEmpty() || link.isEmpty() || image.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "⚠ Please fill in all fields.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String checkAcademy = "SELECT * FROM Academies WHERE name = ?";
            PreparedStatement checkAcadStmt = conn.prepareStatement(checkAcademy);
            checkAcadStmt.setString(1, name);
            ResultSet rs1 = checkAcadStmt.executeQuery();
            if (rs1.next()) {
                JOptionPane.showMessageDialog(this, "❌ Academy name already exists.");
                return;
            }

            String checkUser = "SELECT * FROM Users WHERE username = ?";
            PreparedStatement checkUserStmt = conn.prepareStatement(checkUser);
            checkUserStmt.setString(1, username);
            ResultSet rs2 = checkUserStmt.executeQuery();
            if (rs2.next()) {
                JOptionPane.showMessageDialog(this, "❌ Username is already taken.");
                return;
            }

            conn.setAutoCommit(false);

            String academySql = "INSERT INTO Academies (name, image_url, registration_link) VALUES (?, ?, ?)";
            PreparedStatement academyStmt = conn.prepareStatement(academySql, Statement.RETURN_GENERATED_KEYS);
            academyStmt.setString(1, name);
            academyStmt.setString(2, image);
            academyStmt.setString(3, link);
            academyStmt.executeUpdate();

            ResultSet rs = academyStmt.getGeneratedKeys();
            if (rs.next()) {
                int academyId = rs.getInt(1);

                String userSql = "INSERT INTO Users (username, password_hash, role, academy_id) VALUES (?, ?, ?, ?)";
                PreparedStatement userStmt = conn.prepareStatement(userSql);
                userStmt.setString(1, username);
                userStmt.setString(2, md5Hash(password));
                userStmt.setString(3, role);
                userStmt.setInt(4, academyId);
                userStmt.executeUpdate();

                conn.commit();
                JOptionPane.showMessageDialog(this, "✅ Academy and user created successfully.");

                if (parentPage != null) {
                    parentPage.loadAcademies();
                }

                dispose();
            } else {
                conn.rollback();
                JOptionPane.showMessageDialog(this, "❌ Failed to retrieve academy ID.");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "❌ Error inserting into database.");
        }
    }

    private String md5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }
}

