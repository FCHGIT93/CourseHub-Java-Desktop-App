package CoursesHub2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URI;
import java.sql.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import javax.swing.Timer;
import java.util.ArrayList;
import java.util.List;

public class ViewApplicantPage extends JFrame {

    private List<AnimatedCircle> circles = new ArrayList<>();
    private Timer animationTimer;
    private JFrame parentFrame;

    public ViewApplicantPage(int appId, JFrame parentFrame) {
        this.parentFrame = parentFrame;

        setTitle("Applicant Details");
        setIconImage(new ImageIcon("src/images/bg_logo.png").getImage());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        Color bgColor = new Color(255, 247, 214);
        Color textColor = new Color(0, 51, 102);
        Color borderColor = new Color(0, 51, 102);
        Color inputTextColor = new Color(255, 140, 0);

        JPanel animatedBackground = new JPanel(null) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                for (AnimatedCircle circle : circles) {
                    circle.draw(g);
                }
            }
        };
        animatedBackground.setBackground(bgColor);

        JLabel imageLabel = new JLabel();
        ImageIcon icon = new ImageIcon("src/images/search.png");
        Image img = icon.getImage().getScaledInstance(750, 750, Image.SCALE_SMOOTH);
        imageLabel.setIcon(new ImageIcon(img));
        imageLabel.setBounds(1300, 150, 750, 750);
        animatedBackground.add(imageLabel);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(255, 247, 214, 200));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));
        contentPanel.setBounds(400, 80, 1000, 920);

        JLabel title = new JLabel(" Applicant Details", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 48));
        title.setForeground(textColor);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(title);
        contentPanel.add(Box.createVerticalStrut(20));

        JPanel headerStrip = new JPanel(null);
        headerStrip.setOpaque(false);
        headerStrip.setPreferredSize(new Dimension(1000, 210));
        contentPanel.add(headerStrip);

        JLabel photoLabel = new JLabel();
        photoLabel.setBounds(10, 10, 180, 180);
        headerStrip.add(photoLabel);

        JLabel nameBig = new JLabel("", JLabel.LEFT);
        nameBig.setFont(new Font("Segoe UI", Font.BOLD, 36));
        nameBig.setForeground(textColor);
        nameBig.setBounds(210, 60, 750, 60);
        headerStrip.add(nameBig);

        contentPanel.add(Box.createVerticalStrut(10));

        String applicantEmail = null;
        String firstname = "";
        String lastname = "";
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM JobApplications WHERE app_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, appId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                firstname = rs.getString("firstname");
                lastname  = rs.getString("lastname");
                nameBig.setText(firstname + " " + lastname);

                String photoPath = null;
                try {
                    photoPath = rs.getString("photo_path"); 
                } catch (SQLException ignore) {  }

                ImageIcon profile = buildRoundedProfileIcon(
                        (photoPath != null && !photoPath.trim().isEmpty()) ? photoPath.trim() : null,
                        180
                );
                photoLabel.setIcon(profile);

                contentPanel.add(makeAnimatedInfoBox("First Name", firstname, textColor, borderColor, inputTextColor));
                contentPanel.add(makeAnimatedInfoBox("Last Name",  lastname,  textColor, borderColor, inputTextColor));
                contentPanel.add(makeAnimatedInfoBox("Email",      rs.getString("email"), textColor, borderColor, inputTextColor));
                contentPanel.add(makeAnimatedInfoBox("Phone",      rs.getString("phone"), textColor, borderColor, inputTextColor));
                contentPanel.add(makeAnimatedInfoBox("Domain",     rs.getString("domain"), textColor, borderColor, inputTextColor));
                contentPanel.add(makeAnimatedInfoBox("Experience", rs.getInt("experience_years") + " years", textColor, borderColor, inputTextColor));
                contentPanel.add(makeAnimatedInfoBox("Skills",     rs.getString("skills"), textColor, borderColor, inputTextColor));

                applicantEmail = rs.getString("email");

                String cvPath = rs.getString("cv_path");
                JButton btnOpenCV = new JButton(" Open CV");
                btnOpenCV.setBackground(textColor);
                btnOpenCV.setForeground(Color.WHITE);
                btnOpenCV.setFont(new Font("Segoe UI", Font.BOLD, 30));
                btnOpenCV.setFocusPainted(false);
                btnOpenCV.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                btnOpenCV.setAlignmentX(Component.CENTER_ALIGNMENT);
                btnOpenCV.setPreferredSize(new Dimension(250, 60));
                btnOpenCV.addActionListener(e -> {
                    try {
                        Desktop.getDesktop().open(new File(cvPath));
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "❌ Failed to open CV.");
                        ex.printStackTrace();
                    }
                });

                contentPanel.add(Box.createVerticalStrut(30));
                contentPanel.add(btnOpenCV);
            } else {
                contentPanel.add(makeLabel("❌ Applicant not found.", Color.RED));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            contentPanel.add(makeLabel("❌ Error retrieving data.", Color.RED));
        }

        if (applicantEmail != null) {
            JButton btnContact = new JButton("Contact");
            btnContact.setFont(new Font("Segoe UI", Font.BOLD, 26));
            btnContact.setBackground(new Color(0, 102, 204));
            btnContact.setForeground(Color.WHITE);
            btnContact.setFocusPainted(false);
            btnContact.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnContact.setAlignmentX(Component.CENTER_ALIGNMENT);

            String finalEmail = applicantEmail;
            String finalFirstname = firstname;
            String finalLastname = lastname;

            btnContact.addActionListener(e -> {
                String subject = "Interview Invitation - " + finalFirstname + " " + finalLastname;
                String body =
                        "Hello " + finalFirstname + " " + finalLastname + ",\n\n"
                      + "We have reviewed your CV and were genuinely impressed by your background and skills.\n"
                      + "We would be delighted to invite you for an interview at our office to discuss a potential opportunity with our team.\n\n"
                      + "Kindly reply to this email to confirm your availability.\n"
                      + "Once we hear back from you, we will provide the exact date, time, and location of the interview.\n\n"
                      + "Best regards,\n"
                      + "[Your Academy Name]";
                openEmailInGmail(finalEmail, subject, body);
            });

            contentPanel.add(Box.createVerticalStrut(20));
            contentPanel.add(btnContact);
        }

        JButton btnBack = new JButton(" Back");
        btnBack.setFont(new Font("Segoe UI", Font.BOLD, 24));
        btnBack.setBackground(textColor);
        btnBack.setForeground(Color.WHITE);
        btnBack.setFocusPainted(false);
        btnBack.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBack.setPreferredSize(new Dimension(200, 50));
        btnBack.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnBack.addActionListener(e -> {
            this.dispose();
            if (parentFrame != null) {
                parentFrame.setVisible(true);
            } else {
                new ApplicantsListPage();
            }
        });

        contentPanel.add(Box.createVerticalStrut(30));
        contentPanel.add(btnBack);

        animatedBackground.add(contentPanel);
        add(animatedBackground);
        startCircleAnimation(getWidth(), getHeight());
        setVisible(true);
    }

    private ImageIcon buildRoundedProfileIcon(String path, int size) {
        try {
            BufferedImage src;
            if (path != null && new File(path).exists()) {
                src = ImageIO.read(new File(path));
            } else {
                File ph = new File("src/images/user_placeholder.png");
                if (ph.exists()) {
                    src = ImageIO.read(ph);
                } else {
                    src = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g = src.createGraphics();
                    g.setColor(new Color(220, 220, 220));
                    g.fillRect(0, 0, size, size);
                    g.setColor(new Color(180, 180, 180));
                    g.fillOval(size/4, size/6, size/2, size/2);
                    g.dispose();
                }
            }

            Image scaled = src.getScaledInstance(size, size, Image.SCALE_SMOOTH);
            BufferedImage round = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = round.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Shape circle = new java.awt.geom.Ellipse2D.Double(0, 0, size, size);
            g2.setClip(circle);
            g2.drawImage(scaled, 0, 0, null);

            g2.setClip(null);
            g2.setStroke(new BasicStroke(4f));
            g2.setColor(new Color(0, 51, 102));
            g2.drawOval(2, 2, size - 4, size - 4);
            g2.dispose();

            return new ImageIcon(round);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ImageIcon(new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB));
        }
    }

    private JPanel makeAnimatedInfoBox(String fieldName, String fieldValue, Color labelColor, Color borderColor, Color valueColor) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(borderColor, 3, true));
        panel.setMaximumSize(new Dimension(950, 70));

        JLabel label = new JLabel("  " + fieldName + ": ");
        label.setFont(new Font("Segoe UI", Font.BOLD, 26));
        label.setForeground(labelColor);
        panel.add(label, BorderLayout.WEST);

        JLabel value = new JLabel(fieldValue);
        value.setFont(new Font("Segoe UI", Font.BOLD, 26));
        value.setForeground(new Color(valueColor.getRed(), valueColor.getGreen(), valueColor.getBlue(), 0));
        value.setHorizontalAlignment(SwingConstants.LEFT);
        panel.add(value, BorderLayout.CENTER);

        Timer timer = new Timer(50, new ActionListener() {
            private int alpha = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                alpha += 20;
                if (alpha > 255) alpha = 255;
                value.setForeground(new Color(valueColor.getRed(), valueColor.getGreen(), valueColor.getBlue(), alpha));
                panel.repaint();
                if (alpha >= 255) ((Timer) e.getSource()).stop();
            }
        });
        timer.start();

        return panel;
    }

    private void startCircleAnimation(int width, int height) {
        for (int i = 0; i < 40; i++) {
            circles.add(new AnimatedCircle(width, height));
        }
        animationTimer = new Timer(30, e -> {
            for (AnimatedCircle circle : circles) {
                circle.move();
            }
            repaint();
        });
        animationTimer.start();
    }

    private JLabel makeLabel(String text, Color textColor) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        label.setForeground(textColor);
        label.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return label;
    }

    private void openEmailInGmail(String to, String subject, String body) {
        try {
            String encTo   = URIEncoder.encode(to);
            String encSub  = URIEncoder.encode(subject);
            String encBody = URIEncoder.encode(body).replace("%0A", "%0D%0A"); // CRLF

            String gmail = "https://mail.google.com/mail/?view=cm"
                         + "&to="   + encTo
                         + "&su="   + encSub
                         + "&body=" + encBody;

            boolean opened = false;

            if (isWindows()) {
                try {
                    new ProcessBuilder("cmd", "/c", "start", "chrome", "\"" + gmail + "\"").start();
                    opened = true;
                } catch (Exception ignore) {}
                if (!opened) {
                    try {
                        new ProcessBuilder("cmd", "/c", "start", "", "\"" + gmail + "\"").start();
                        opened = true;
                    } catch (Exception ignore) {}
                }
            }

            // --- macOS ---
            if (!opened && isMac()) {
                try {
                    new ProcessBuilder("open", gmail).start();
                    opened = true;
                } catch (Exception ignore) {}
            }

            // --- Linux ---
            if (!opened && isLinux()) {
                try {
                    new ProcessBuilder("xdg-open", gmail).start();
                    opened = true;
                } catch (Exception ignore) {}
            }

            // --- Java Desktop 
            if (!opened && Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                try {
                    Desktop.getDesktop().browse(new URI(gmail));
                    opened = true;
                } catch (Exception ignore) {}
            }

            if (!opened) {
                copyToClipboard(gmail);
                JOptionPane.showMessageDialog(this,
                        "Couldn't open Gmail automatically.\nThe compose link was copied. Paste it in your browser.",
                        "Open Gmail", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "❌ Couldn't open Gmail.\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean isWindows() { return System.getProperty("os.name").toLowerCase().contains("win"); }
    private boolean isMac()     { return System.getProperty("os.name").toLowerCase().contains("mac"); }
    private boolean isLinux()   { return System.getProperty("os.name").toLowerCase().contains("nux")
                                       || System.getProperty("os.name").toLowerCase().contains("nix"); }

    private void copyToClipboard(String text) {
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new java.awt.datatransfer.StringSelection(text), null);
        } catch (Exception ignore) {}
    }
}

class URIEncoder {
    public static String encode(String input) {
        try {
            return java.net.URLEncoder.encode(input, "UTF-8");
        } catch (Exception e) {
            return input;
        }
    }
}

class AnimatedCircle {
    private int x, y, dx, dy, radius;
    private Color color;

    public AnimatedCircle(int panelWidth, int panelHeight) {
        radius = 20 + (int) (Math.random() * 20);
        x = (int) (Math.random() * panelWidth);
        y = (int) (Math.random() * panelHeight);
        dx = (int) (Math.random() * 4 + 1);
        dy = (int) (Math.random() * 4 + 1);
        color = Math.random() > 0.5 ? new Color(0, 51, 102, 50) : new Color(255, 153, 0, 50);
    }

    public void move() {
        x += dx;
        y += dy;
        if (x < 0 || x > 1800) dx = -dx;
        if (y < 0 || y > 1000) dy = -dy;
    }

    public void draw(Graphics g) {
        g.setColor(color);
        g.fillOval(x, y, radius * 2, radius * 2);
    }
}
