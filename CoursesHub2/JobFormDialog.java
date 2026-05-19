
package CoursesHub2;
;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JobFormDialog extends JDialog {

    public enum Mode { CREATE, EDIT }

    // Theme
    private static final Color BLUE     = new Color(0, 51, 100);
    private static final Color ORANGE   = new Color(255, 140, 0);
    private static final Color RED      = new Color(180, 40, 40);
    private static final Color CARD_BG  = new Color(255, 244, 200);
    private static final Font  TITLE_F  = new Font("Segoe UI", Font.BOLD, 26);
    private static final Font  LABEL_F  = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font  INPUT_F  = new Font("Segoe UI", Font.PLAIN, 18);

    private final Mode   mode;
    private final String userRole;
    private final int    currentAcademyId;
    private final Job    editingJobOrNull;
    private final Runnable onSaved; // callback  refresh (ً ViewJobsPage::loadJobs)

    // UI
    private JComboBox<AcademyItem> cbAcademy; //ـfor admin
    private JTextField tfDomain, tfExp, tfAge, tfSkills, tfSalary;

    public JobFormDialog(Frame owner,
                         Mode mode,
                         String userRole,
                         int currentAcademyId,
                         Job jobOrNull,
                         Runnable onSavedOrNull) {
        super(owner, true);
        this.mode = mode;
        this.userRole = userRole == null ? "guest" : userRole.trim().toLowerCase();
        this.currentAcademyId = currentAcademyId;
        this.editingJobOrNull = jobOrNull;
        this.onSaved = onSavedOrNull;

        setTitle(mode == Mode.CREATE ? "Add Job"
                : ("Edit Job #" + (jobOrNull != null ? jobOrNull.getJobId() : "")));
        setIconImage(new ImageIcon("src/images/bg_logo.png").getImage());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(820, 620);
        setLocationRelativeTo(owner);
        setResizable(false);

        setContentPane(buildUI());
        installShortcuts();

        if (mode == Mode.EDIT && editingJobOrNull != null) {
            prefill(editingJobOrNull);
        }
    }

    private JPanel buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(255, 247, 214));

        // Header
        JLabel title = new JLabel(getTitle(), SwingConstants.CENTER);
        title.setFont(TITLE_F);
        title.setForeground(BLUE);
        title.setBorder(new EmptyBorder(18, 18, 18, 18));
        root.add(title, BorderLayout.NORTH);

        // Card
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 170, 70), 3),
                new EmptyBorder(18, 22, 18, 22)
        ));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(10, 12, 10, 12);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0;

        // Academy (admin فقط)
        if (isAdmin()) {
            card.add(makeLabel("Academy:"), gc);
            gc.gridx = 1; gc.weightx = 1;
            cbAcademy = new JComboBox<>(loadAcademies().toArray(new AcademyItem[0]));
            stylizeInput(cbAcademy);
            card.add(cbAcademy, gc);
            gc.gridx = 0; gc.gridy++; gc.weightx = 0;
        }

        // Domain
        card.add(makeLabel("Domain:"), gc);
        gc.gridx = 1; gc.weightx = 1;
        tfDomain = new JTextField();
        stylizeInput(tfDomain);
        card.add(tfDomain, gc);
        gc.gridx = 0; gc.gridy++; gc.weightx = 0;

        // Min Experience
        card.add(makeLabel("Min Experience (yrs):"), gc);
        gc.gridx = 1; gc.weightx = 1;
        tfExp = new JTextField();
        stylizeInput(tfExp);
        card.add(tfExp, gc);
        gc.gridx = 0; gc.gridy++; gc.weightx = 0;

        // Min Age
        card.add(makeLabel("Min Age:"), gc);
        gc.gridx = 1; gc.weightx = 1;
        tfAge = new JTextField();
        stylizeInput(tfAge);
        card.add(tfAge, gc);
        gc.gridx = 0; gc.gridy++; gc.weightx = 0;

        // Salary
        card.add(makeLabel("Salary ($):"), gc);
        gc.gridx = 1; gc.weightx = 1;
        tfSalary = new JTextField();
        stylizeInput(tfSalary);
        card.add(tfSalary, gc);
        gc.gridx = 0; gc.gridy++; gc.weightx = 0;

        // Skills
        card.add(makeLabel("Required Skills:"), gc);
        gc.gridx = 1; gc.weightx = 1;
        tfSkills = new JTextField();
        stylizeInput(tfSkills);
        card.add(tfSkills, gc);

        // Actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        actions.setOpaque(false);

        JButton btnCancel = makeHollowButton("Cancel");
        btnCancel.addActionListener(e -> dispose());
        actions.add(btnCancel);

        if (mode == Mode.EDIT) {
           

            JButton btnUpdate = makeSolidButton("Update", ORANGE, Color.WHITE);
            btnUpdate.addActionListener(e -> doSaveOrUpdate());
            actions.add(btnUpdate);

            // Enter = Update
            getRootPane().setDefaultButton(btnUpdate);
        } else {
            JButton btnCreate = makeSolidButton("Create", ORANGE, Color.WHITE);
            btnCreate.addActionListener(e -> doSaveOrUpdate());
            actions.add(btnCreate);

            // Enter = Create
            getRootPane().setDefaultButton(btnCreate);
        }

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        GridBagConstraints wgc = new GridBagConstraints();
        wgc.insets = new Insets(20, 26, 10, 26);
        wgc.fill = GridBagConstraints.HORIZONTAL;
        wgc.weightx = 1; wgc.gridx = 0; wgc.gridy = 0;
        centerWrapper.add(card, wgc);

        wgc.gridy = 1; wgc.insets = new Insets(0, 26, 18, 26);
        centerWrapper.add(actions, wgc);

        root.add(centerWrapper, BorderLayout.CENTER);
        return root;
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(LABEL_F);
        l.setForeground(BLUE);
        return l;
    }

    private void stylizeInput(JComponent c) {
        c.setFont(INPUT_F);
        c.setForeground(BLUE);
        c.setBackground(Color.WHITE);
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BLUE, 2, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
    }

    private JButton makeSolidButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 18));
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return b;
    }

    private JButton makeHollowButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 18));
        b.setBackground(Color.WHITE);
        b.setForeground(BLUE);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createLineBorder(BLUE, 2, true));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(new Color(235, 242, 255)); }
            @Override public void mouseExited (MouseEvent e) { b.setBackground(Color.WHITE); }
        });
        return b;
    }

    private boolean isAdmin()   { return "admin".equalsIgnoreCase(userRole); }
    private boolean isAcademy() { return "academy".equalsIgnoreCase(userRole); }

    private void prefill(Job j) {
        if (isAdmin() && cbAcademy != null) {
            for (int i = 0; i < cbAcademy.getItemCount(); i++) {
                if (cbAcademy.getItemAt(i).id == j.getAcademyId()) {
                    cbAcademy.setSelectedIndex(i);
                    break;
                }
            }
        }
        tfDomain.setText(safe(j.getDomain()));
        tfExp.setText(String.valueOf(j.getMinExperience()));
        tfAge.setText(String.valueOf(j.getMinAge()));
        tfSkills.setText(safe(j.getRequiredSkills()));
        tfSalary.setText(String.valueOf(j.getSalary()));
    }

    private void installShortcuts() {
       
        String action = "PRIMARY_ACTION";
        int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(); 
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_S, mask), action);
        getRootPane().getActionMap().put(action, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { doSaveOrUpdate(); }
        });

        // ESC 
        String cancel = "CANCEL";
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), cancel);
        getRootPane().getActionMap().put(cancel, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { dispose(); }
        });
    }

    private void doSaveOrUpdate() {
        // Validation
        String domain = tfDomain.getText().trim();
        String skills = tfSkills.getText().trim();
        int minExp, minAge; double salary;

        if (domain.isEmpty()) { warn("Domain is required."); tfDomain.requestFocus(); return; }

        try { minExp = tfExp.getText().trim().isEmpty() ? 0 : Integer.parseInt(tfExp.getText().trim()); }
        catch (Exception e) { warn("Min Experience must be a number."); tfExp.requestFocus(); return; }

        try { minAge = tfAge.getText().trim().isEmpty() ? 0 : Integer.parseInt(tfAge.getText().trim()); }
        catch (Exception e) { warn("Min Age must be a number."); tfAge.requestFocus(); return; }

        try {
            String s = tfSalary.getText().trim();
            salary = s.isEmpty() ? 0.0 : Double.parseDouble(s);
            if (salary < 0) throw new IllegalArgumentException();
        } catch (Exception e) { warn("Salary must be a non-negative number."); tfSalary.requestFocus(); return; }

        int academyId;
        if (isAdmin()) {
            AcademyItem it = (AcademyItem) cbAcademy.getSelectedItem();
            if (it == null) { warn("Select an academy."); return; }
            academyId = it.id;
        } else {
            academyId = currentAcademyId;
        }

        if (mode == Mode.CREATE) {
            insertJob(academyId, domain, minExp, minAge, skills, salary);
        } else {
            if (editingJobOrNull == null) { warn("No job selected."); return; }
            int effectiveAcademyId = isAdmin() ? academyId : editingJobOrNull.getAcademyId();
            updateJob(editingJobOrNull.getJobId(), effectiveAcademyId, domain, minExp, minAge, skills, salary);
        }
    }

  

    private void insertJob(int academyId, String domain, int minExp, int minAge, String skills, double salary) {
        final String SQL =
                "INSERT INTO Jobs(academy_id, domain, min_experience, min_age, required_skills, salary) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, academyId);
            ps.setString(2, domain);
            ps.setInt(3, minExp);
            ps.setInt(4, minAge);
            ps.setString(5, skills);
            ps.setDouble(6, salary);
            int n = ps.executeUpdate();
            if (n > 0) {
                info("Created ✓");
                if (onSaved != null) onSaved.run();
                dispose();
            } else {
                warn("Create failed.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            error("DB error: " + ex.getMessage());
        }
    }

    private void updateJob(int jobId, int academyIdEffective,
                           String domain, int minExp, int minAge, String skills, double salary) {
        String SQL = "UPDATE Jobs SET domain=?, min_experience=?, min_age=?, required_skills=?, salary=?, academy_id=? " +
                     "WHERE job_id=? " + (isAcademy() ? "AND academy_id=?" : "");
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setString(1, domain);
            ps.setInt(2, minExp);
            ps.setInt(3, minAge);
            ps.setString(4, skills);
            ps.setDouble(5, salary);
            ps.setInt(6, academyIdEffective); 
            ps.setInt(7, jobId);
            if (isAcademy()) ps.setInt(8, academyIdEffective);

            int n = ps.executeUpdate();
            if (n > 0) {
                info("Updated ✓");
                if (onSaved != null) onSaved.run();
                dispose();
            } else {
                warn("Update failed (permission or not found).");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            error("DB error: " + ex.getMessage());
        }
    }

    private void deleteJobWithNotifications(int jobId, int academyIdIfAcademy) {
        final String SQL_DEL_NOTIFS = "DELETE FROM Notifications WHERE job_id = ?";
        final String SQL_DEL_JOB    = "DELETE FROM Jobs WHERE job_id = ?" + (isAcademy() ? " AND academy_id = ?" : "");
        try (Connection conn = DBConnection.getConnection()) {
            boolean old = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try (PreparedStatement psN = conn.prepareStatement(SQL_DEL_NOTIFS);
                 PreparedStatement psJ = conn.prepareStatement(SQL_DEL_JOB)) {

                psN.setInt(1, jobId);
                int deletedN = psN.executeUpdate();

                psJ.setInt(1, jobId);
                if (isAcademy()) psJ.setInt(2, academyIdIfAcademy);
                int delJob = psJ.executeUpdate();

                if (delJob > 0) {
                    conn.commit();
                    info("Deleted ✓\nNotifications removed: " + deletedN);
                    if (onSaved != null) onSaved.run();
                    dispose();
                } else {
                    conn.rollback();
                    warn("Delete failed (permission or not found).");
                }
            } catch (Exception ex) {
                try { conn.rollback(); } catch (Exception ignore) {}
                throw ex;
            } finally {
                conn.setAutoCommit(old);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            error("DB error: " + ex.getMessage());
        }
    }

    //  Utilities
    private List<AcademyItem> loadAcademies() {
        List<AcademyItem> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT academy_id, name FROM Academies ORDER BY name ASC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new AcademyItem(rs.getInt(1), rs.getString(2)));
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        return list;
    }

    private void warn(String m)  { JOptionPane.showMessageDialog(this, m, "Warning", JOptionPane.WARNING_MESSAGE); }
    private void info(String m)  { JOptionPane.showMessageDialog(this, m, "Info", JOptionPane.INFORMATION_MESSAGE); }
    private void error(String m) { JOptionPane.showMessageDialog(this, m, "Error", JOptionPane.ERROR_MESSAGE); }

    private static String safe(String s) { return s == null ? "" : s; }

    private static class AcademyItem {
        final int id; final String name;
        AcademyItem(int id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }
}
