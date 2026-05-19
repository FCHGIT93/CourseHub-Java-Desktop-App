package CoursesHub2;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.sql.*;
import java.util.*;
import java.util.List;


public class AIAdvisorPage extends JFrame {

    // ===== UI =====
    private JComboBox<String> academyCombo;
    private final Map<String, Integer> academyMap = new LinkedHashMap<String, Integer>();

    private JButton btnGenerate;
    private JButton btnSubmit;

    private JPanel quizPanel;
    private JScrollPane quizScroll;
    private JTextArea adviceArea;

    // Bottom bar (Back + Status)
    private JLabel statusLabel;
    private JButton backBtn;

    // ===== Data =====
    private final OpenAIService ai = new OpenAIService();

    private final List<OpenAIService.InterestQuestion> questions = new ArrayList<OpenAIService.InterestQuestion>();
    private final List<ButtonGroup> answerGroups = new ArrayList<ButtonGroup>();

    public AIAdvisorPage() {
        setTitle("AI Course Advisor");
        setIconImage(new ImageIcon("src/images/bg_logo.png").getImage());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());

        // ===== Top =====
        JPanel top = new JPanel(new GridBagLayout());
        top.setBorder(new EmptyBorder(12, 16, 12, 16));
        top.setBackground(new Color(255, 247, 214));

        JLabel lblAcademy = new JLabel("Academy:");
        lblAcademy.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblAcademy.setForeground(new Color(0, 51, 100));

        academyCombo = new JComboBox<String>();
        academyCombo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        academyCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) clearSurvey();
        });

        btnGenerate = new JButton("Generate Interest Survey");
        btnGenerate.setBackground(new Color(0, 102, 204));
        btnGenerate.setForeground(Color.WHITE);
        btnGenerate.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnGenerate.setFocusPainted(false);
        btnGenerate.addActionListener(e -> generateSurvey());

        btnSubmit = new JButton("Submit & Get Advice");
        btnSubmit.setBackground(new Color(255, 140, 0));
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnSubmit.setFocusPainted(false);
        btnSubmit.setEnabled(false);
        btnSubmit.addActionListener(e -> gradeAndRecommend());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        top.add(lblAcademy, gbc);

        gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        top.add(academyCombo, gbc);

        gbc.gridx = 2; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        top.add(btnGenerate, gbc);

        gbc.gridx = 3;
        top.add(btnSubmit, gbc);

        add(top, BorderLayout.NORTH);

        // ===== Center =====
        JPanel center = new JPanel(new GridLayout(1, 2, 16, 0));
        center.setBorder(new EmptyBorder(10, 16, 16, 16));
        center.setBackground(Color.WHITE);

        quizPanel = new JPanel();
        quizPanel.setLayout(new BoxLayout(quizPanel, BoxLayout.Y_AXIS));
        quizPanel.setBackground(Color.WHITE);

        quizScroll = new JScrollPane(quizPanel);
        quizScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(255,170,70), 2),
                "Interest Survey",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 16),
                new Color(0,51,100)
        ));
        center.add(quizScroll);

        JPanel advicePanel = new JPanel(new BorderLayout());
        advicePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0,51,100), 2),
                "AI Recommendations",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 16),
                new Color(0,51,100)
        ));

        adviceArea = new JTextArea();
        adviceArea.setEditable(false);
        adviceArea.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        adviceArea.setForeground(new Color(0, 51, 100));
        adviceArea.setLineWrap(true);
        adviceArea.setWrapStyleWord(true);

        advicePanel.add(new JScrollPane(adviceArea), BorderLayout.CENTER);
        center.add(advicePanel);

        add(center, BorderLayout.CENTER);

        // ===== Bottom bar: Back (left) + Status (right) =====
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBorder(new EmptyBorder(8, 12, 8, 12));
        bottomBar.setBackground(new Color(255, 247, 214));

        JPanel leftWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftWrap.setOpaque(false);

        backBtn = new JButton("Back");
        backBtn.setBackground(new Color(0, 51, 100));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        backBtn.setFocusPainted(false);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.setPreferredSize(new Dimension(110, 40));
        backBtn.addActionListener(e -> {
            try {
                // رجِّع نفس نافذة الداشبورد بدون إنشاء وحدة جديدة
                AdminDashboard dash = AdminDashboard.getInstance();
                if (dash != null) dash.setVisible(true);
            } catch (Throwable ignore) {}
            dispose();
        });

        leftWrap.add(backBtn);
        bottomBar.add(leftWrap, BorderLayout.WEST);

        statusLabel = new JLabel(" ");
        JPanel rightWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightWrap.setOpaque(false);
        rightWrap.add(statusLabel);
        bottomBar.add(rightWrap, BorderLayout.EAST);

        add(bottomBar, BorderLayout.SOUTH);

        // Load academies
        loadAcademies();
        setVisible(true);

        if (!ai.isConfigured()) {
            System.out.println("NOTE: OPENAI_API_KEY not set. Local generator in use.");
        }
    }

    private void setStatus(String s) { statusLabel.setText(s); }

    private void clearSurvey() {
        questions.clear();
        answerGroups.clear();
        quizPanel.removeAll();
        quizPanel.revalidate();
        quizPanel.repaint();
        adviceArea.setText("");
        btnSubmit.setEnabled(false);
        setStatus("Ready");
    }

    private void loadAcademies() {
        academyMap.clear();
        academyCombo.removeAllItems();
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT academy_id, name FROM Academies ORDER BY name")) {
            while (rs.next()) {
                int id = rs.getInt("academy_id");
                String name = rs.getString("name");
                academyMap.put(name, Integer.valueOf(id));
                academyCombo.addItem(name);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB error loading academies: " + ex.getMessage());
        }
        if (academyCombo.getItemCount() > 0) academyCombo.setSelectedIndex(0);
    }

    private Integer getSelectedAcademyId() {
        String name = (String) academyCombo.getSelectedItem();
        if (name == null) return null;
        return academyMap.get(name);
    }
    private String getSelectedAcademyName() {
        Object name = academyCombo.getSelectedItem();
        return name == null ? "" : name.toString();
    }

    private List<String> loadCoursesForAcademy(int academyId) {
        List<String> list = new ArrayList<String>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT course_name FROM Courses WHERE academy_id=? ORDER BY course_name")) {
            ps.setInt(1, academyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(rs.getString("course_name"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    private List<String> loadCatalogForAcademy(int academyId, int limit) {
        List<String> cat = new ArrayList<String>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT TOP " + limit + " course_name, ISNULL(description,'') AS d " +
                             "FROM Courses WHERE academy_id=? ORDER BY course_name")) {
            ps.setInt(1, academyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String cn = rs.getString("course_name");
                    String d  = rs.getString("d");
                    cat.add(cn + (d == null || d.trim().isEmpty() ? "" : (": " + d)));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return cat;
    }

    // ===== Actions =====

    private void generateSurvey() {
        Integer aid = getSelectedAcademyId();
        if (aid == null) {
            JOptionPane.showMessageDialog(this, "Please select an academy first.");
            return;
        }
        List<String> courses = loadCoursesForAcademy(aid.intValue());
        if (courses.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No courses found for this academy.");
            return;
        }

        setStatus("Generating survey…");
        btnGenerate.setEnabled(false);
        btnSubmit.setEnabled(false);
        questions.clear();
        answerGroups.clear();
        quizPanel.removeAll();

        SwingWorker<List<OpenAIService.InterestQuestion>, Void> worker =
                new SwingWorker<List<OpenAIService.InterestQuestion>, Void>() {
                    @Override
                    protected List<OpenAIService.InterestQuestion> doInBackground() throws Exception {
                        return ai.generateInterestSurvey(getSelectedAcademyName(), courses, 12);
                    }

                    @Override
                    protected void done() {
                        btnGenerate.setEnabled(true);
                        try {
                            List<OpenAIService.InterestQuestion> qs = get();
                            if (qs == null || qs.isEmpty()) {
                                JOptionPane.showMessageDialog(AIAdvisorPage.this, "No questions generated.");
                                setStatus("No questions");
                                return;
                            }
                            questions.addAll(qs);
                            renderSurvey(questions);
                            btnSubmit.setEnabled(true);
                            setStatus("Survey ready");
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(AIAdvisorPage.this,
                                    "Failed to generate survey: " + ex.getMessage(),
                                    "AI Error", JOptionPane.ERROR_MESSAGE);
                            setStatus("Failed");
                        }
                    }
                };
        worker.execute();
    }

    private void renderSurvey(List<OpenAIService.InterestQuestion> qs) {
        quizPanel.removeAll();
        answerGroups.clear();

        for (int i = 0; i < qs.size(); i++) {
            OpenAIService.InterestQuestion q = qs.get(i);

            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBackground(Color.WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(230,230,230)),
                    new EmptyBorder(10, 12, 10, 12)
            ));

            JLabel qLabel = new JLabel("Q" + (i + 1) + ". " + q.question);
            qLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
            qLabel.setForeground(new Color(0, 51, 100));
            card.add(qLabel);
            card.add(Box.createVerticalStrut(6));

            ButtonGroup group = new ButtonGroup();
            answerGroups.add(group);

            for (int j = 0; j < q.options.size(); j++) {
                OpenAIService.InterestQuestion.Option opt = q.options.get(j);
                JRadioButton rb = new JRadioButton((char)('A' + j) + ") " + opt.label);
                rb.setOpaque(false);
                rb.setFont(new Font("Segoe UI", Font.PLAIN, 15));
                rb.setForeground(new Color(0, 51, 100));
                group.add(rb);
                card.add(rb);
            }

            quizPanel.add(card);
            quizPanel.add(Box.createVerticalStrut(8));
        }

        quizPanel.revalidate();
        quizPanel.repaint();
    }

    private void gradeAndRecommend() {
        if (questions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No survey to grade.");
            return;
        }

        Map<String,Integer> courseVotes = new LinkedHashMap<String,Integer>();
        int answered = 0;

        for (int i = 0; i < questions.size(); i++) {
            OpenAIService.InterestQuestion q = questions.get(i);
            ButtonGroup g = answerGroups.get(i);

            int selIdx = -1;
            Enumeration<AbstractButton> e = g.getElements();
            int idx = 0;
            while (e.hasMoreElements()) {
                AbstractButton b = e.nextElement();
                if (b.isSelected()) { selIdx = idx; break; }
                idx++;
            }
            if (selIdx >= 0 && selIdx < q.options.size()) {
                answered++;
                OpenAIService.InterestQuestion.Option opt = q.options.get(selIdx);
                String course = (opt.courseName == null ? "Unknown Course" : opt.courseName);
                Integer count = courseVotes.get(course);
                courseVotes.put(course, (count == null ? 1 : count + 1));
            }
        }

        if (answered == 0) {
            JOptionPane.showMessageDialog(this, "Please answer at least one question.");
            return;
        }

        Map<String,Double> interestPct = new LinkedHashMap<String,Double>();
        for (Map.Entry<String,Integer> en : courseVotes.entrySet()) {
            double pct = 100.0 * en.getValue().intValue() / (double) answered;
            interestPct.put(en.getKey(), Double.valueOf(pct));
        }

        
        Integer aid = getSelectedAcademyId();
        List<String> catalog = (aid == null) ? Collections.<String>emptyList() : loadCatalogForAcademy(aid.intValue(), 12);

        final int answeredF = answered;
        final Map<String,Double> interestPctF = new LinkedHashMap<String,Double>(interestPct);
        final String academyNameF = getSelectedAcademyName();
        final List<String> catalogF = new ArrayList<String>(catalog);

        btnSubmit.setEnabled(false);
        setStatus(String.format(java.util.Locale.US,
                "Calculating recommendations… Answers considered: %d", answeredF));

        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return ai.recommendCourses(academyNameF, interestPctF, catalogF);
            }

            @Override
            protected void done() {
                btnSubmit.setEnabled(true);
                try {
                    String advice = get();

                    StringBuilder sb = new StringBuilder(900);
                    sb.append("Considered answers: ").append(answeredF).append("\n\n");
                    sb.append("Per-course interest:\n");
                    for (Map.Entry<String,Double> en : interestPctF.entrySet()) {
                        sb.append(" - ").append(en.getKey())
                          .append(": ")
                          .append(String.format(java.util.Locale.US, "%.1f%%", en.getValue().doubleValue()))
                          .append("\n");
                    }
                    sb.append("\n").append(advice);

                    adviceArea.setText(sb.toString());
                    setStatus("Done");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    adviceArea.setText("Failed to get advice: " + ex.getMessage());
                    setStatus("Failed");
                }
            }
        };
        worker.execute();
    }
}
