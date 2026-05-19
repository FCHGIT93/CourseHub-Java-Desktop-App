package CoursesHub2;

import java.awt.*;
import java.awt.event.*;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.util.ArrayList;
import javax.swing.border.AbstractBorder;
import java.security.MessageDigest;
import java.sql.*;

public class AdminLogin extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private int failedAttempts = 0;
    private boolean lockedOut = false;
    private Timer lockoutTimer;

    private static final Color MAIN_BLUE     = new Color(0, 51, 100);
    private static final Color ACCENT_ORANGE = new Color(255, 186, 0);
    private static final Color BG_BEIGE      = new Color(255, 247, 214);

    public AdminLogin() {
        setTitle("CourseHub - Admin Login");
        setIconImage(new ImageIcon("src/images/bg_logo.png").getImage());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(false);

        BackgroundPanel mainPanel = new BackgroundPanel();
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel logoLabel = new JLabel();
        ImageIcon logoIcon = new ImageIcon("src/images/bg_logo.png");
        Image scaledImage = logoIcon.getImage().getScaledInstance(500, 500, Image.SCALE_SMOOTH);
        logoLabel.setIcon(new ImageIcon(scaledImage));

        // ===== Username =====
        usernameField = new JTextField(20);
        usernameField.setText("Username");
        usernameField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (usernameField.getText().equals("Username")) {
                    usernameField.setText("");
                }
            }
            public void focusLost(FocusEvent e) {
                if (usernameField.getText().isEmpty()) {
                    usernameField.setText("Username");
                }
            }
        });
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 30));
        usernameField.setForeground(MAIN_BLUE);
        usernameField.setBackground(new Color(255, 255, 240));
        usernameField.setCaretColor(MAIN_BLUE);
        usernameField.setOpaque(false);
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(50, MAIN_BLUE), new EmptyBorder(15, 25, 15, 25)));
        usernameField.setPreferredSize(new Dimension(500, 70));
        passwordField = new JPasswordField(20);
        passwordField.setEchoChar((char)0);
        passwordField.setText("Password");
        passwordField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (new String(passwordField.getPassword()).equals("Password")) {
                    passwordField.setText("");
                    passwordField.setEchoChar('•');
                }
            }
            public void focusLost(FocusEvent e) {
                if (new String(passwordField.getPassword()).isEmpty()) {
                    passwordField.setText("Password");
                    passwordField.setEchoChar((char)0);
                }
            }
        });
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 30));
        passwordField.setForeground(MAIN_BLUE);
        passwordField.setBackground(new Color(255, 255, 240));
        passwordField.setCaretColor(MAIN_BLUE);
        passwordField.setOpaque(false);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(50, MAIN_BLUE), new EmptyBorder(15, 25, 15, 25)));
        passwordField.setPreferredSize(new Dimension(500, 70));
        JButton loginBtn = new JButton("LOGIN") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(255, 140, 0) : ACCENT_ORANGE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 32));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setContentAreaFilled(false);
        loginBtn.setOpaque(false);
        loginBtn.setPreferredSize(new Dimension(500, 70));
        loginBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        JButton skipBtn = new JButton("SKIP") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? MAIN_BLUE.darker() : MAIN_BLUE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        skipBtn.setFont(new Font("Segoe UI", Font.BOLD, 28));
        skipBtn.setForeground(Color.WHITE);
        skipBtn.setFocusPainted(false);
        skipBtn.setContentAreaFilled(false);
        skipBtn.setOpaque(false);
        skipBtn.setPreferredSize(new Dimension(500, 62));
        skipBtn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        skipBtn.setToolTipText("Skip login and open AdminDashboard (guest/demo)");
        ActionListener loginAction = e -> {
            if (lockedOut) {
                JOptionPane.showMessageDialog(null, "Too many failed attempts. Try again later.", "Locked Out", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String user = usernameField.getText();
            String pass = new String(passwordField.getPassword());
            if (user.equals("Username") || pass.equals("Password")) {
                JOptionPane.showMessageDialog(null, "Please enter valid credentials.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT * FROM Users WHERE username = ? AND password_hash = ?")) {
                ps.setString(1, user);
                ps.setString(2, hashPassword(pass));
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String role = rs.getString("role");
                    int academyId = rs.getInt("academy_id");
                    dispose();
                    SwingUtilities.invokeLater(() -> new AdminDashboard(role, academyId));
                } else {
                    failedAttempts++;
                    if (failedAttempts >= 3) {
                        lockedOut = true;
                        JOptionPane.showMessageDialog(null, "3 failed attempts. Locked for 3 minutes.", "Locked Out", JOptionPane.ERROR_MESSAGE);
                        lockoutTimer = new Timer();
                        lockoutTimer.schedule(new TimerTask() {
                            public void run() {
                                lockedOut = false;
                                failedAttempts = 0;
                            }
                        }, 3 * 60 * 1000);
                    } else {
                        JOptionPane.showMessageDialog(null, "Invalid credentials!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        };

        ActionListener skipAction = e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new AdminDashboard("guest", -1));
        };

        loginBtn.addActionListener(loginAction);
        usernameField.addActionListener(loginAction);
        passwordField.addActionListener(loginAction);
        skipBtn.addActionListener(skipAction);
        gbc.insets = new Insets(20, 0, 20, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(logoLabel, gbc);
        gbc.gridy++;
        mainPanel.add(usernameField, gbc);
        gbc.gridy++;
        mainPanel.add(passwordField, gbc);
        gbc.gridy++;
        mainPanel.add(loginBtn, gbc);
        gbc.gridy++;
        gbc.insets = new Insets(5, 0, 20, 0);
        mainPanel.add(skipBtn, gbc);

        setContentPane(mainPanel);
        setVisible(true);
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    class RoundedBorder extends AbstractBorder {
        private int radius;
        private Color color;

        public RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(x + 1, y + 1, width - 3, height - 3, radius, radius);
        }
    }
    class BackgroundPanel extends JPanel {
        private java.util.List<Block> blocks = new ArrayList<>();
        private javax.swing.Timer animationTimer;

        public BackgroundPanel() {
            setBackground(BG_BEIGE);
            for (int i = 0; i < 16; i++) {
                blocks.add(new Block());
            }

            animationTimer = new javax.swing.Timer(25, e -> {
                for (Block b : blocks) {
                    b.update(getWidth(), getHeight());
                }
                repaint();
            });
            animationTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            for (int i = 0; i < blocks.size(); i++) {
                Block b = blocks.get(i);

                Color c = b.white
                        ? new Color(255, 255, 255, 180)
                        : new Color(255, 186, 0, 120);

                g2.setColor(c);
                g2.fillRoundRect(b.x, b.y, b.size, b.size, 22, 22);
                g2.setStroke(new BasicStroke(2f));
                g2.setColor(new Color(255, 255, 255, b.white ? 90 : 40));
                g2.drawRoundRect(b.x, b.y, b.size, b.size, 22, 22);
            }
            g2.dispose();
        }

        class Block {
            int x, y, size, speedY, speedX;
            boolean white;

            public Block() {
                size = (int) (120 + Math.random() * 100);
                x = (int) (Math.random() * 1600);
                y = (int) (Math.random() * 900);
                speedY = 2 + (int) (Math.random() * 3);          
                speedX = (int) (-1 + Math.random() * 3);         
                white = Math.random() < 0.5;
            }

            public void update(int panelWidth, int panelHeight) {
                y -= speedY;
                x += speedX;

                if (y + size < 0) {
                    y = panelHeight + size + (int)(Math.random()*200);
                    x = (int) (Math.random() * Math.max(panelWidth, 1));
                    size = (int) (120 + Math.random() * 100);
                    speedY = 2 + (int) (Math.random() * 3);
                    speedX = (int) (-1 + Math.random() * 3);
                    white = Math.random() < 0.5;
                }

                if (x < -size) x = panelWidth;
                if (x > panelWidth) x = -size;
            }
        }
    }

    public static void main(String[] args) {
        new AdminLogin();
    }
}
