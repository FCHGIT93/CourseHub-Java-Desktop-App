package CoursesHub2;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;

public class AdminDashboard extends JFrame {
    private JPanel sidebar, contentPanel, videoControlsPanel;
    private EmbeddedMediaPlayerComponent mediaPlayerComponent;
    private boolean isPlaying = false;
    private String userRole;
    private int currentAcademyId;
    private JButton stopVideoBtn, backBtn;

    private static AdminDashboard instance;
    private JButton btnNotifications;

   
    private int lastUnreadCount = -1;

    private JLabel chatIconLabel;
    private final int CHAT_SIZE = 160;
    private final int CHAT_MARGIN = 10;
    private final int CHAT_SHIFT_LEFT = 1150;
    private final int CHAT_SHIFT_UP   = 70;

    public AdminDashboard(String role, int academyId) {
        instance = this;
        this.userRole = role == null ? "" : role.toLowerCase();
        this.currentAcademyId = academyId;

        setTitle("Welcome To CourseHub");
        setIconImage(new ImageIcon("src/images/bg_logo.png").getImage());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());

        createSidebar();
        createContentPanel();

        if (canShowAI()) {
            installChatIcon();
        }
        SwingUtilities.invokeLater(this::refreshNotificationsBadge);

        addWindowFocusListener(new WindowAdapter() {
            @Override public void windowGainedFocus(WindowEvent e) {
                SwingUtilities.invokeLater(() -> refreshNotificationsBadge());
            }
        });

        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) { repositionChatIcon(); }
            @Override public void componentShown(ComponentEvent e)   { repositionChatIcon(); }
        });

        setVisible(true);
    }

    public static AdminDashboard getInstance() {
        return instance;
    }

    public String getUserRole() {
        return userRole;
    }
    public int getCurrentAcademyId() {
        return currentAcademyId;
    }

    private boolean isAdmin()   { return "admin".equalsIgnoreCase(userRole); }
    private boolean isAcademy() { return "academy".equalsIgnoreCase(userRole); }
    private boolean isGuest()   { return "guest".equalsIgnoreCase(userRole); }
    private boolean canShowAI() { return isAdmin() || isGuest(); } // AI icon فقط للأدمن أو الضيف (Skip)

    private void createSidebar() {
        sidebar = new JPanel();
        sidebar.setBackground(new Color(255, 247, 214));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(550, getHeight()));

        JLabel logo = new JLabel();
        ImageIcon icon = new ImageIcon("src/images/bg_logo.png");
        Image scaled = icon.getImage().getScaledInstance(350, 350, Image.SCALE_SMOOTH);
        logo.setIcon(new ImageIcon(scaled));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        sidebar.add(Box.createVerticalStrut(40));
        sidebar.add(logo);
        sidebar.add(Box.createVerticalStrut(20));

        sidebar.add(createSidebarButton("View Introduction", e -> playIntroVideo()));
        sidebar.add(createSidebarButton("View Academies", e -> {
            this.setVisible(false);
            new ViewAcademyPage(userRole, currentAcademyId, this);
        }));

        if (isAdmin() || isGuest()) {
            sidebar.add(createSidebarButton("Find Courses", e -> openCourseSearchPage()));
        }

        if (isAdmin()) {
            sidebar.add(createSidebarButton("Apply for Job", e -> new ApplyForJobPage()));
            sidebar.add(createSidebarButton("View Applicant Job", e -> new ApplicantsListPage()));
            sidebar.add(createSidebarButton("Add Job", e -> new AddJobPage(userRole, currentAcademyId)));
            sidebar.add(createSidebarButton("View Available Jobs", e -> new ViewJobsPage(userRole, currentAcademyId)));

            btnNotifications = createSidebarButton("Notifications", e -> {
                new NotificationPage(currentAcademyId, userRole);
            });
            sidebar.add(btnNotifications);

            sidebar.add(createSidebarButton("Add Academy", e -> new AddAcademyPage(null)));
            sidebar.add(createSidebarButton("Delete Academy", e -> new DeleteAcademyPage(this)));
           
            sidebar.add(createSidebarButton("Edit Academy", e -> {
    try {
       
        EditAcademyDialog dlg = new EditAcademyDialog(this, this::refreshNotificationsBadge);

        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true); 
    } catch (Throwable t) {
        t.printStackTrace();
        JOptionPane.showMessageDialog(this,
            "Couldn't open Edit Academy.\n" + t.getMessage(),
            "Error", JOptionPane.ERROR_MESSAGE);
    }
}));


        } else if (isAcademy()) {
           
            sidebar.add(createSidebarButton("View Applicant Job", e -> new ApplicantsListPage()));
            sidebar.add(createSidebarButton("Add Job", e -> new AddJobPage(userRole, currentAcademyId)));
            sidebar.add(createSidebarButton("View Available Jobs", e -> new ViewJobsPage(userRole, currentAcademyId)));
            btnNotifications = createSidebarButton("Notifications", e -> {
                new NotificationPage(currentAcademyId, userRole);
            });
            sidebar.add(btnNotifications);

        } else if (isGuest()) {
            sidebar.add(createSidebarButton("Apply for Job", e -> new ApplyForJobPage()));
            sidebar.add(createSidebarButton("View Available Jobs", e -> new ViewJobsPage(userRole, currentAcademyId)));
            sidebar.add(createSidebarButton("Back", e -> {
                dispose();
                new AdminLogin();
            }));

        } else {
            sidebar.add(createSidebarButton("Apply for Job", e -> new ApplyForJobPage()));
            sidebar.add(createSidebarButton("View Available Jobs", e -> new ViewJobsPage(userRole, currentAcademyId)));
            sidebar.add(createSidebarButton("Back", e -> {
                dispose();
                new AdminLogin();
            }));
        }

        if (!isGuest()) {
            sidebar.add(createSidebarButton("Logout", e -> {
                dispose();
                new AdminLogin();
            }));
        }

        sidebar.add(Box.createVerticalGlue());
        add(sidebar, BorderLayout.WEST);
    }
    private void openCourseSearchPage() {
        try {
            Class<?> cls = Class.forName("CoursesHub2.CourseSearchPage");
            java.lang.reflect.Constructor<?> ctor = cls.getConstructor();
            ctor.newInstance();
        } catch (Throwable t) {
            JOptionPane.showMessageDialog(
                this,
                "Course search coming soon.\n(أنشئ الكلاس CoursesHub2.CourseSearchPage لتفعيل الميزة)",
                "Find Courses",
                JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    private JButton createSidebarButton(String text, ActionListener action) {
        JButton button = new JButton(text);
        button.setMaximumSize(new Dimension(500, 200));
        button.setPreferredSize(new Dimension(380, 70));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFont(new Font("Segoe UI", Font.BOLD, 24));
        button.setBackground(new Color(0, 51, 90));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addActionListener(action);
        return button;
    }

    private void createContentPanel() {
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);

        JLabel imageLabel = new JLabel();
        File successImage = new File("src/images/success.png");
        if (successImage.exists()) {
            ImageIcon successIcon = new ImageIcon(successImage.getAbsolutePath());
            Image scaledSuccess = successIcon.getImage().getScaledInstance(1400, 1000, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaledSuccess));
        } else {
            imageLabel.setText("Image not found: success.png");
            imageLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        }
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        contentPanel.add(imageLabel, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);
    }

    private void playIntroVideo() {
        hideChatIcon();

        remove(sidebar);
        contentPanel.removeAll();

        mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
        contentPanel.add(mediaPlayerComponent, BorderLayout.CENTER);

        videoControlsPanel = new JPanel();
        Color buttonColor = new Color(0, 51, 90);

        stopVideoBtn = new JButton("Stop Video");
        stopVideoBtn.setBackground(buttonColor);
        stopVideoBtn.setForeground(Color.WHITE);

        backBtn = new JButton("Back");
        backBtn.setBackground(buttonColor);
        backBtn.setForeground(Color.WHITE);

        stopVideoBtn.addActionListener(e -> stopVideo());
        backBtn.addActionListener(e -> stopVideoAndReturn());

        videoControlsPanel.add(stopVideoBtn);
        videoControlsPanel.add(backBtn);
        contentPanel.add(videoControlsPanel, BorderLayout.NORTH);

        revalidate();
        repaint();

        mediaPlayerComponent.mediaPlayer().media().play(new File("src/videos/intro.mp4").getAbsolutePath());
        isPlaying = true;
    }

    private void stopVideo() {
        if (mediaPlayerComponent != null && isPlaying) {
            mediaPlayerComponent.mediaPlayer().controls().stop();
            isPlaying = false;
        }
    }

    private void stopVideoAndReturn() {
        stopVideo();
        remove(contentPanel);
        createContentPanel();
        add(sidebar, BorderLayout.WEST);
        revalidate();
        repaint();

        SwingUtilities.invokeLater(this::refreshNotificationsBadge);
        SwingUtilities.invokeLater(this::repositionChatIcon);

        if (canShowAI()) showChatIcon();
    }
    private void showChatIcon() {
        if (chatIconLabel != null) chatIconLabel.setVisible(true);
    }
    private void hideChatIcon() {
        if (chatIconLabel != null) chatIconLabel.setVisible(false);
    }

    private void playNotificationSound() {
        try {
            File soundFile = new File("src/sounds/notification.wav");
            if (!soundFile.exists()) return;
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refreshNotificationsBadge() {
        if (btnNotifications == null) return;

        if (!"academy".equalsIgnoreCase(userRole)) {
            btnNotifications.setText("Notifications");
            lastUnreadCount = -1;
            return;
        }

        int unread = 0;
        try {
            unread = NotificationService.getUnreadCountForAcademy(currentAcademyId);
        } catch (Throwable ignore) { }

        if (lastUnreadCount != -1 && unread > lastUnreadCount) {
            playNotificationSound();
        }
        lastUnreadCount = unread;

        if (unread > 0) {
            btnNotifications.setText(
                "<html>Notifications&nbsp;<span style='color:#d00000;font-weight:bold'>(" + unread + ")</span></html>"
            );
        } else {
            btnNotifications.setText("Notifications");
        }
    }

    private void installChatIcon() {
        ImageIcon icon = loadScaledIcon("src/images/chatai.png", CHAT_SIZE);
        if (icon == null) {
            icon = loadFallbackBubble(CHAT_SIZE);
        }

        chatIconLabel = new JLabel(icon);
        chatIconLabel.setSize(CHAT_SIZE, CHAT_SIZE);
        chatIconLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        chatIconLabel.setToolTipText("AI Test");

        chatIconLabel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                new AIAdvisorPage();
            }
        });

        getLayeredPane().add(chatIconLabel, JLayeredPane.POPUP_LAYER);
        repositionChatIcon();
    }

    private void repositionChatIcon() {
        if (chatIconLabel == null) return;
        Insets ins = getInsets();

        int x = getWidth()  - ins.right  - CHAT_SIZE - CHAT_MARGIN - CHAT_SHIFT_LEFT;
        int y = getHeight() - ins.bottom - CHAT_SIZE - CHAT_MARGIN - CHAT_SHIFT_UP;

        chatIconLabel.setBounds(x, y, CHAT_SIZE, CHAT_SIZE);
        getLayeredPane().revalidate();
        getLayeredPane().repaint();
    }

    private ImageIcon loadScaledIcon(String path, int size) {
        try {
            File f = new File(path);
            if (!f.exists()) return null;
            Image src = new ImageIcon(f.getAbsolutePath()).getImage();
            BufferedImage out = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = out.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,   RenderingHints.VALUE_ANTIALIAS_ON);
            g2.drawImage(src, 0, 0, size, size, null);
            g2.dispose();
            return new ImageIcon(out);
        } catch (Exception e) {
            return null;
        }
    }

    private ImageIcon loadFallbackBubble(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(0, 51, 100));
        g2.fillRoundRect(4, 6, size-8, size-14, size/3, size/3);
        int[] xs = { size/2, size/2 - 10, size/2 + 2 };
        int[] ys = { size-4, size-16, size-12 };
        g2.fillPolygon(xs, ys, 3);

        g2.setColor(Color.WHITE);
        int r = 6, cx = size/2 - 14, cy = size/2 - 6, d = 14;
        g2.fillOval(cx, cy, r, r);
        g2.fillOval(cx + d, cy, r, r);
        g2.fillOval(cx + 2*d, cy, r, r);

        g2.dispose();
        return new ImageIcon(img);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminDashboard("admin", -1));
    }
}
