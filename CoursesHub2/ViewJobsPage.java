package CoursesHub2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ViewJobsPage extends JFrame {
    private JPanel carouselPanel;
    private JTextField searchField;
    private List<Job> allJobs = new ArrayList<>();
    private int currentIndex = 0;
    private final int CARDS_PER_VIEW = 3;

    // ===== Session (role + academy) =====
    private String userRole = "guest";
    private int currentAcademyId = -1;

    private boolean isAdmin()   { return "admin".equalsIgnoreCase(userRole); }
    private boolean isAcademy() { return "academy".equalsIgnoreCase(userRole); }
    private boolean isGuest()   { return "guest".equalsIgnoreCase(userRole); }

    // ===== Animated Background Panel =====
    static class AnimatedBackgroundPanel extends JPanel {
        private static final int FPS = 45, BUBBLE_COUNT = 28;
        private static final Color BABY_ORANGE_A = new Color(255, 205, 150);
        private static final Color BABY_ORANGE_B = new Color(255, 187, 120);
        private static final Color BABY_YELLOW_A = new Color(255, 246, 190);
        private static final Color BABY_YELLOW_B = new Color(255, 240, 170);

        private static class Bubble {
            float x, y, r, dx, dy, dr, alphaBase, alphaPhase;
            Bubble(float x, float y, float r, float dx, float dy, float dr, float alphaBase, float alphaPhase) {
                this.x=x; this.y=y; this.r=r; this.dx=dx; this.dy=dy; this.dr=dr; this.alphaBase=alphaBase; this.alphaPhase=alphaPhase;
            }
        }

        private final java.util.List<Bubble> bubbles = new ArrayList<>();
        private final Random rnd = new Random();
        private javax.swing.Timer timer;
        private boolean initialized = false;
        private float phase = 0f;

        AnimatedBackgroundPanel() {
            setOpaque(true);
            setLayout(new BorderLayout());
            timer = new javax.swing.Timer(1000/FPS, e -> { update(); repaint(); });
            timer.start();
            addComponentListener(new ComponentAdapter() {
                @Override public void componentResized(ComponentEvent e) {
                    if (getWidth()>10 && getHeight()>10) {
                        if (!initialized) { initBubbles(); initialized = true; }
                        else {
                            int w=getWidth(), h=getHeight();
                            for (int i=0;i<bubbles.size();i+=3) bubbles.set(i, randomBubble(w,h));
                        }
                    }
                }
            });
        }

        private void initBubbles() {
            bubbles.clear();
            int w = Math.max(1, getWidth()), h = Math.max(1, getHeight());
            for (int i=0;i<BUBBLE_COUNT;i++) bubbles.add(randomBubble(w,h));
        }
        private Bubble randomBubble(int w, int h) {
            float r  = 50 + rnd.nextFloat()*260f;
            float x  = -220 + rnd.nextFloat()*(w + 440);
            float y  = -220 + rnd.nextFloat()*(h + 440);
            float dx = (-0.35f + rnd.nextFloat()*0.70f);
            float dy = (rnd.nextBoolean() ? 0.7f : -0.7f) + (-0.25f + rnd.nextFloat()*0.50f);
            float dr = (-0.50f + rnd.nextFloat()*1.00f);
            float alphaBase  = 0.36f + rnd.nextFloat()*0.28f;
            float alphaPhase = rnd.nextFloat()*(float)Math.PI*2;
            return new Bubble(x,y,r,dx,dy,dr,alphaBase,alphaPhase);
        }

        private static Color lerpColor(Color a, Color b, float t) {
            t = Math.max(0f, Math.min(1f, t));
            int r  = (int)(a.getRed()   + (b.getRed()   - a.getRed())   * t);
            int g  = (int)(a.getGreen() + (b.getGreen() - a.getGreen()) * t);
            int bl = (int)(a.getBlue()  + (b.getBlue()  - a.getBlue())  * t);
            return new Color(r, g, bl);
        }

        private void update() {
            if (!initialized && getWidth()>10 && getHeight()>10) { initBubbles(); initialized = true; }
            if (!initialized) return;

            int w = getWidth(), h = getHeight();
            phase += 0.006f;

            for (Bubble b: bubbles) {
                b.x += b.dx; b.y += b.dy; b.r += b.dr;

                float margin = 240f;
                if (b.x < -margin || b.x > w+margin) b.dx = -b.dx;
                if (b.y < -margin || b.y > h+margin) b.dy = -b.dy;

                if (b.r < 45)  { b.r = 45;  b.dr = Math.abs(b.dr); }
                if (b.r > 320) { b.r = 320; b.dr = -Math.abs(b.dr); }

                b.alphaPhase += 0.01f;
            }
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w=Math.max(1,getWidth()), h=Math.max(1,getHeight());
            float t = (float)(0.5 + 0.5*Math.sin(phase));
            Color top    = lerpColor(BABY_YELLOW_A, BABY_ORANGE_A, t);
            Color bottom = lerpColor(BABY_YELLOW_B, BABY_ORANGE_B, 1f - t);
            g2.setPaint(new GradientPaint(0,0, top, w,h, bottom));
            g2.fillRect(0,0,w,h);

            if (!initialized) { g2.dispose(); return; }

            for (Bubble b: bubbles) {
                float dynamicAlpha = Math.min(1f, b.alphaBase * (0.90f + 0.10f*(float)Math.sin(b.alphaPhase)));
                int aCenter = Math.min(255, Math.round(dynamicAlpha * 255));
                int aMid    = Math.min(210, Math.round(dynamicAlpha * 255 * 0.82f));

                float[] dist = {0f, 0.25f, 1f};
                Color[] cols = {
                    new Color(255,255,255, aCenter),
                    new Color(255,255,255, aMid),
                    new Color(255,255,255, 0)
                };
                RadialGradientPaint rgp = new RadialGradientPaint(
                    new Point2D.Float(b.x, b.y), b.r, dist, cols);
                g2.setPaint(rgp);

                int d = Math.round(2*b.r), x = Math.round(b.x - b.r), y = Math.round(b.y - b.r);
                g2.fillOval(x, y, d, d);

                Composite old = g2.getComposite();
                g2.setComposite(AlphaComposite.SrcOver.derive(0.08f));
                g2.setColor(Color.WHITE);
                int inset = Math.round(b.r * 0.08f);
                g2.fillOval(x + inset, y + inset, d - 2*inset, d - 2*inset);
                g2.setComposite(old);

                g2.setStroke(new BasicStroke(1.8f));
                g2.setColor(new Color(255,255,255, 110));
                g2.drawOval(x, y, d, d);
            }
            g2.dispose();
        }
    }
    // ===== End Animated Background =====

    // ===== Constructors =====
    public ViewJobsPage() { this("guest", -1); }

    public ViewJobsPage(String role, int academyId) {
        this.userRole = (role == null ? "guest" : role.trim().toLowerCase());
        this.currentAcademyId = academyId;

        setTitle("Available Jobs");
        setIconImage(new ImageIcon("src/images/bg_logo.png").getImage());
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        AnimatedBackgroundPanel bg = new AnimatedBackgroundPanel();
        setContentPane(bg);

        // Top
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));
        topPanel.setOpaque(false);

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        searchLabel.setForeground(new Color(0, 51, 100));

        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 18));

        JButton searchBtn = new JButton("Go");
        searchBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        searchBtn.setBackground(new Color(0, 102, 204));
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setFocusPainted(false);
        searchBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setOpaque(false);
        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchBtn, BorderLayout.EAST);

        topPanel.add(searchPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        searchBtn.addActionListener(e -> filterJobs());
        searchField.addActionListener(e -> filterJobs());

        // Wrapper + Carousel
        JPanel wrapperPanel = new JPanel(new GridBagLayout());
        wrapperPanel.setOpaque(false);
        carouselPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 36, 36));
        carouselPanel.setOpaque(false);
        carouselPanel.setPreferredSize(new Dimension(1400, 650));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1; gbc.gridy = 0; gbc.anchor = GridBagConstraints.CENTER;
        wrapperPanel.add(carouselPanel, gbc);

        JButton prevBtn = new JButton("←");
        prevBtn.setFont(new Font("Segoe UI", Font.BOLD, 28));
        prevBtn.setPreferredSize(new Dimension(56, 56));
        prevBtn.setBackground(new Color(15, 36, 64));
        prevBtn.setForeground(Color.WHITE);
        prevBtn.setFocusPainted(false);
        prevBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        prevBtn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        prevBtn.setOpaque(true);
        prevBtn.addActionListener(e -> showPrevious());
        gbc.gridx = 0; wrapperPanel.add(prevBtn, gbc);

        JButton nextBtn = new JButton("→");
        nextBtn.setFont(new Font("Segoe UI", Font.BOLD, 28));
        nextBtn.setPreferredSize(new Dimension(56, 56));
        nextBtn.setBackground(new Color(15, 36, 64));
        nextBtn.setForeground(Color.WHITE);
        nextBtn.setFocusPainted(false);
        nextBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        nextBtn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        nextBtn.setOpaque(true);
        nextBtn.addActionListener(e -> showNext());
        gbc.gridx = 2; wrapperPanel.add(nextBtn, gbc);

        add(wrapperPanel, BorderLayout.CENTER);

        // Bottom
        JButton backBtn = new JButton("← Back");
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        backBtn.setBackground(new Color(0, 51, 100));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.setPreferredSize(new Dimension(120, 40));
        backBtn.addActionListener(e -> dispose());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setOpaque(false);
        bottomPanel.add(backBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        loadJobs();
        setVisible(true);
    }

    private void loadJobs() {
        allJobs.clear();

        String baseSql =
            "SELECT j.*, a.name AS academy_name " +
            "FROM Jobs j JOIN Academies a ON j.academy_id = a.academy_id ";

        boolean filterByAcademy = isAcademy() && currentAcademyId > 0;
        String order = " ORDER BY j.created_at DESC";
        String sql = baseSql + (filterByAcademy ? "WHERE j.academy_id = ? " : "") + order;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (filterByAcademy) {
                ps.setInt(1, currentAcademyId);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Job job = new Job(
                    rs.getInt("job_id"),
                    rs.getInt("academy_id"),
                    rs.getString("academy_name"),
                    rs.getString("domain"),
                    rs.getInt("min_experience"),
                    rs.getInt("min_age"),
                    rs.getString("required_skills"),
                    rs.getDouble("salary"),
                    rs.getTimestamp("created_at")
                );
                allJobs.add(job);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Safety UI filter
        if (isAcademy() && currentAcademyId > 0) {
            List<Job> ownOnly = new ArrayList<>();
            for (Job j : allJobs) if (j.getAcademyId() == currentAcademyId) ownOnly.add(j);
            allJobs = ownOnly;
        }

        currentIndex = 0;
        displayJobs();
    }

   private void filterJobs() {
    String q = searchField.getText().trim();
    if (q.isEmpty()) { loadJobs(); return; }

    String[] tokens = q.toLowerCase().split("\\s+");
    List<Job> filtered = new ArrayList<>();
    for (Job job : allJobs) {
        if (matchesAllTokens(job, tokens)) {
            filtered.add(job);
        }
    }
    allJobs = filtered;
    currentIndex = 0;
    displayJobs();
}

private boolean matchesAllTokens(Job job, String[] tokens) {
    for (String t : tokens) {
        if (!matchesToken(job, t)) return false;
    }
    return true;
}

private boolean matchesToken(Job job, String t) {
    // field:prefixed search
    if (t.startsWith("age:"))      return matchesIntField(job.getMinAge(), t.substring(4));
    if (t.startsWith("exp:") || t.startsWith("experience:"))
                                   return matchesIntField(job.getMinExperience(), t.contains(":") ? t.substring(t.indexOf(':')+1) : "");
    if (t.startsWith("salary:"))   return matchesSalary(job.getSalary(), t.substring(7));
    if (t.startsWith("domain:"))   return containsSafe(job.getDomain(), t.substring(7));
    if (t.startsWith("academy:"))  return containsSafe(job.getAcademyName(), t.substring(8));
    if (t.startsWith("skills:"))   return containsSafe(job.getRequiredSkills(), t.substring(7));

    // generic text search across text fields
    if (containsSafe(job.getDomain(), t)) return true;
    if (containsSafe(job.getAcademyName(), t)) return true;
    if (containsSafe(job.getRequiredSkills(), t)) return true;

    // numeric tokens: match age / experience / salary (rounded) / posted year
    if (isDigits(t)) {
        try {
            int n = Integer.parseInt(t);
            if (job.getMinAge() == n) return true;
            if (job.getMinExperience() == n) return true;
            if (Math.round(job.getSalary()) == n) return true;
            if (job.getCreatedAt() != null && job.getCreatedAt().toLocalDateTime().getYear() == n) return true;
        } catch (NumberFormatException ignored) {}
    }

    // also try partial numeric match inside salary/int fields as strings
    if (String.valueOf(job.getMinAge()).contains(t)) return true;
    if (String.valueOf(job.getMinExperience()).contains(t)) return true;
    if (String.valueOf((long)Math.round(job.getSalary())).contains(t)) return true;

    return false;
}

private boolean containsSafe(String field, String token) {
    return field != null && token != null && field.toLowerCase().contains(token.toLowerCase());
}

private boolean matchesIntField(int value, String token) {
    token = token == null ? "" : token.trim();
    if (token.isEmpty()) return false;
    if (isDigits(token)) {
        try { return Integer.parseInt(token) == value; }
        catch (NumberFormatException ignored) {}
    }
    // fallback: contains
    return String.valueOf(value).contains(token);
}

private boolean matchesSalary(double salary, String token) {
    token = token == null ? "" : token.replaceAll("[^0-9]", "");
    if (token.isEmpty()) return false;
    long rounded = Math.round(salary);
    try {
        int n = Integer.parseInt(token);
        if (rounded == n) return true;
    } catch (NumberFormatException ignored) {}
    return String.valueOf(rounded).contains(token);
}

private boolean isDigits(String s) {
    return s != null && s.matches("\\d+");
}

    private void displayJobs() {
        carouselPanel.removeAll();

        int end = Math.min(currentIndex + CARDS_PER_VIEW, allJobs.size());
        for (int i=currentIndex; i<end; i++) {
            Job job = allJobs.get(i);

            JPanel card = new JPanel(new BorderLayout());
            card.setPreferredSize(new Dimension(420, 520));
            card.setBackground(new Color(255, 244, 200));
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 170, 70), 3),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
            ));

            JLabel title = new JLabel(job.getAcademyName(), SwingConstants.CENTER);
            title.setFont(new Font("Segoe UI", Font.BOLD, 22));
            title.setForeground(new Color(255, 120, 0));
            title.setBorder(BorderFactory.createEmptyBorder(4, 6, 8, 6));
            card.add(title, BorderLayout.NORTH);

            String posted = (job.getCreatedAt()!=null)
                    ? job.getCreatedAt().toLocalDateTime().toLocalDate().toString()
                    : "";
            String html =
                "<html><body style='font-family: Segoe UI; color: rgb(0,51,100); font-size: 19px;'>" +
                "<div style='width:360px;'>" +
                "<b style='color:rgb(0,51,100)'>Domain:</b> " + htmlEscape(safe(job.getDomain())) + "<br/>" +
                "<b style='color:rgb(0,51,100)'>Experience:</b> " + job.getMinExperience() + " yrs<br/>" +
                "<b style='color:rgb(0,51,100)'>Min Age:</b> " + job.getMinAge() + "<br/>" +
                "<b style='color:rgb(0,51,100)'>Skills:</b> " + htmlEscape(safe(job.getRequiredSkills())) + "<br/>" +
                "<b style='color:rgb(0,51,100)'>Salary:</b> $" + job.getSalary() + "<br/>" +
                "<b style='color:rgb(0,51,100)'>Posted:</b> " + posted +
                "</div></body></html>";

            JEditorPane body = new JEditorPane("text/html", html);
            body.setEditable(false);
            body.setOpaque(false);
            body.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
            card.add(body, BorderLayout.CENTER);

            // ===== Buttons Row =====
            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
            buttons.setOpaque(false);

            // Apply: admin & guest  (NOT academy)
            if (isAdmin() || isGuest()) {
                JButton applyBtn = makeBtn("Apply", new Color(0, 102, 204), Color.WHITE);
                applyBtn.addActionListener(e -> new ApplyForJobPage());
                buttons.add(applyBtn);
            }

            boolean canManage = isAdmin() || (isAcademy() && job.getAcademyId() == currentAcademyId);
            if (canManage) {
                JButton editBtn = makeBtn("Edit", new Color(255, 140, 0), Color.WHITE); // ORANGE
                editBtn.addActionListener(e -> {
                    Frame owner = null;
                    Window w = SwingUtilities.getWindowAncestor(carouselPanel);
                    if (w instanceof Frame) owner = (Frame) w;

                    new JobFormDialog(
                        owner,
                        JobFormDialog.Mode.EDIT,
                        userRole,
                        currentAcademyId,
                        job,
                        this::loadJobs
                    ).setVisible(true);
                });
                buttons.add(editBtn);

                JButton delBtn  = makeBtn("Delete", new Color(192, 0, 0), Color.WHITE); // RED
                delBtn.addActionListener(e -> deleteJob(job));
                buttons.add(delBtn);
            }

            card.add(buttons, BorderLayout.SOUTH);
            carouselPanel.add(card);
        }

        carouselPanel.revalidate();
        carouselPanel.repaint();
    }

    private JButton makeBtn(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
    private void deleteJob(Job job) {
        boolean canDelete = isAdmin() || (isAcademy() && job.getAcademyId() == currentAcademyId);
        if (!canDelete) {
            JOptionPane.showMessageDialog(this, "You don't have permission to delete this job.",
                    "Forbidden", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int ok = JOptionPane.showConfirmDialog(this,
                "Delete job #" + job.getJobId() + " ?\nThis will also remove related notifications.",
                "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;

        final String SQL_DEL_NOTIFS = "DELETE FROM Notifications WHERE job_id = ?";
        final String SQL_DEL_JOB    = "DELETE FROM Jobs WHERE job_id = ?" + (isAcademy() ? " AND academy_id = ?" : "");

        try (Connection conn = DBConnection.getConnection()) {
            boolean oldAuto = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try (PreparedStatement psNotif = conn.prepareStatement(SQL_DEL_NOTIFS);
                 PreparedStatement psJob   = conn.prepareStatement(SQL_DEL_JOB)) {

                psNotif.setInt(1, job.getJobId());
                int cNotifs = psNotif.executeUpdate();

                psJob.setInt(1, job.getJobId());
                if (isAcademy()) psJob.setInt(2, currentAcademyId);
                int cJob = psJob.executeUpdate();

                if (cJob > 0) {
                    conn.commit();

                    allJobs.remove(job);
                    if (currentIndex >= allJobs.size())
                        currentIndex = Math.max(0, allJobs.size() - CARDS_PER_VIEW);
                    displayJobs();

                    JOptionPane.showMessageDialog(this,
                            "Deleted Job #" + job.getJobId() +
                            "\nNotifications removed: " + cNotifs);
                } else {
                    conn.rollback();
                    JOptionPane.showMessageDialog(this,
                            "Delete failed (permission issue or job not found).",
                            "Warn", JOptionPane.WARNING_MESSAGE);
                }

            } catch (Exception ex) {
                try { conn.rollback(); } catch (Exception ignore) {}
                throw ex;
            } finally {
                try { conn.setAutoCommit(oldAuto); } catch (Exception ignore) {}
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String safe(String s) { return (s==null) ? "" : s; }
    private static String htmlEscape(String s) {
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }

    private void showPrevious() {
        if (currentIndex - CARDS_PER_VIEW >= 0) {
            currentIndex -= CARDS_PER_VIEW;
            displayJobs();
        }
    }
    private void showNext() {
        if (currentIndex + CARDS_PER_VIEW < allJobs.size()) {
            currentIndex += CARDS_PER_VIEW;
            displayJobs();
        }
    }
}
