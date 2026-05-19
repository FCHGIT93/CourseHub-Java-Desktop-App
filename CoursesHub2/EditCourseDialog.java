package CoursesHub2;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EditCourseDialog extends JDialog {

    private static final Color BLUE    = new Color(0,51,100);
    private static final Color ORANGE  = new Color(255,140,0);
    private static final Color BG      = new Color(255,247,214);
    private static final Color CARD_BG = new Color(255,244,200);
    private static final Color BORDER  = new Color(255,170,70);

    private static final Font  TITLE   = new Font("Segoe UI", Font.BOLD, 34);
    private static final Font  LABEL   = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font  INPUT   = new Font("Segoe UI", Font.PLAIN, 20);

    private final String role;
    private final int academyIdHint;

    // UI
    private JComboBox<AcademyItem> cbAcademy; // for admin only
    private JComboBox<CourseItem>  cbCourse;

    private JTextField tfName, tfImage, tfStart, tfEnd, tfPrice, tfDuration;
    private JTextArea  taDesc;

    public EditCourseDialog(Frame owner, String role, int academyIdHint) {
        super(owner, "Edit Courses", true);
        this.role = role == null ? "guest" : role.toLowerCase();
        this.academyIdHint = academyIdHint;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setIconImage(new ImageIcon("src/images/bg_logo.png").getImage());
        setResizable(true);

        setContentPane(buildUI());
        installShortcuts();

        // Fullscreen  (JDialog ما فيه setExtendedState)
        maximizeToScreen();

        initData();
    }

    private void maximizeToScreen() {
        Rectangle r = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        setBounds(r);
    }

    private boolean isAdmin()   { return "admin".equalsIgnoreCase(role); }
    private boolean isAcademy() { return "academy".equalsIgnoreCase(role); }

    private JComponent buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);

        JLabel title = new JLabel("Edit Courses", SwingConstants.CENTER);
        title.setFont(TITLE);
        title.setForeground(BLUE);
        title.setBorder(new EmptyBorder(18, 18, 8, 18));
        root.add(title, BorderLayout.NORTH);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 3, true),
                new EmptyBorder(20, 24, 20, 24)
        ));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(10, 12, 10, 12);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill   = GridBagConstraints.HORIZONTAL;

        final int COL_W = 520;  
        final int FLD_H = 44;  

        int row = 0;

        if (isAdmin()) {
            gc.gridx = 0; gc.gridy = row; gc.gridwidth = 1; gc.weightx = 0;
            card.add(makeLabel("Academy:"), gc);

            gc.gridx = 1; gc.gridy = row; gc.gridwidth = 3; gc.weightx = 1;
            cbAcademy = new JComboBox<>();
            stylizeInput(cbAcademy);
            cbAcademy.setPreferredSize(new Dimension(COL_W * 2 + 12, FLD_H));
            cbAcademy.addActionListener(e -> loadCoursesForSelectedAcademy());
            card.add(cbAcademy, gc);
            row++;
        }

        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 1; gc.weightx = 0;
        card.add(makeLabel("Course:"), gc);

        gc.gridx = 1; gc.gridy = row; gc.gridwidth = 3; gc.weightx = 1;
        cbCourse = new JComboBox<>();
        stylizeInput(cbCourse);
        cbCourse.setPreferredSize(new Dimension(COL_W * 2 + 12, FLD_H));
        cbCourse.addActionListener(e -> loadSelectedCourseDetails());
        card.add(cbCourse, gc);
        row++;

        addPair(card, 0, row, "Name:", tfName = new JTextField(), COL_W, FLD_H);

        addPair(card, 2, row, "Image (path/url):", tfImage = new JTextField(), COL_W, FLD_H);

        addPair(card, 0, row, "Start Date (YYYY-MM-DD):", tfStart = new JTextField(), COL_W, FLD_H);
        // End (right)
        addPair(card, 2, row, "End Date (YYYY-MM-DD):", tfEnd = new JTextField(), COL_W, FLD_H);
        row++;

        // Price (left)
        addPair(card, 0, row, "Price ($):", tfPrice = new JTextField(), COL_W, FLD_H);
        // Duration (right)
        addPair(card, 2, row, "Duration:", tfDuration = new JTextField(), COL_W, FLD_H);
        row++;

        // Description 
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 1; gc.weightx = 0;
        card.add(makeLabel("Description:"), gc);

        gc.gridx = 1; gc.gridy = row; gc.gridwidth = 3; gc.weightx = 1;
        taDesc = new JTextArea(6, 20);
        taDesc.setLineWrap(true);
        taDesc.setWrapStyleWord(true);
        taDesc.setFont(INPUT);
        taDesc.setForeground(BLUE);
        taDesc.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BLUE, 2, true),
                new EmptyBorder(10, 12, 10, 12)
        ));
        JScrollPane sp = new JScrollPane(taDesc);
        sp.setPreferredSize(new Dimension(COL_W * 2 + 12, 170));
        card.add(sp, gc);
        row++;

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 6));
        actions.setOpaque(false);
        JButton btnCancel = makeHollowButton("Cancel");
        btnCancel.addActionListener(e -> dispose());
        JButton btnUpdate = makeSolidButton("Update");
        btnUpdate.addActionListener(e -> doUpdate());
        getRootPane().setDefaultButton(btnUpdate);
        actions.add(btnCancel);
        actions.add(btnUpdate);

        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 4; gc.weightx = 1;
        gc.fill = GridBagConstraints.NONE; gc.anchor = GridBagConstraints.EAST;
        card.add(actions, gc);

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(BG);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 24, 18, 24);
        c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 1; c.fill = GridBagConstraints.NONE;
        center.add(card, c);

        root.add(center, BorderLayout.CENTER);
        return root;
    }

    private void addPair(JPanel card, int gridx, int gridy, String label, JTextField field, int w, int h) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(10, 12, 10, 12);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill   = GridBagConstraints.HORIZONTAL;

        // Label
        gc.gridx = gridx;    
        gc.gridy = gridy;
        gc.gridwidth = 1;
        gc.weightx = 0;
        card.add(makeLabel(label), gc);

        // Field
        gc.gridx = gridx + 1; 
        gc.gridy = gridy;
        gc.gridwidth = 1;
        gc.weightx = 1;
        stylizeInput(field);
        field.setPreferredSize(new Dimension(w, h));
        card.add(field, gc);
    }

    private void installShortcuts() {
        String action = "PRIMARY_ACTION";
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), action);
        getRootPane().getActionMap().put(action, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { doUpdate(); }
        });

        String cancel = "CANCEL";
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), cancel);
        getRootPane().getActionMap().put(cancel, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { dispose(); }
        });
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(LABEL);
        l.setForeground(BLUE);
        return l;
    }

    private void stylizeInput(JComponent c) {
        c.setFont(INPUT);
        c.setForeground(BLUE);
        c.setBackground(Color.WHITE);
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BLUE, 2, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
    }

    private JButton makeSolidButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 20));
        b.setBackground(ORANGE);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        return b;
    }

    private JButton makeHollowButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 20));
        b.setBackground(Color.WHITE);
        b.setForeground(BLUE);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createLineBorder(BLUE, 2, true));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(new Color(235,242,255)); }
            @Override public void mouseExited (MouseEvent e) { b.setBackground(Color.WHITE); }
        });
        return b;
    }

    private void initData() {
        if (isAdmin()) {
            DefaultComboBoxModel<AcademyItem> m = new DefaultComboBoxModel<>();
            for (AcademyItem a : loadAcademies()) m.addElement(a);
            cbAcademy.setModel(m);
            if (m.getSize() > 0) {
                int pick = 0;
                if (academyIdHint > 0) {
                    for (int i=0;i<m.getSize();i++) {
                        if (m.getElementAt(i).id == academyIdHint) { pick = i; break; }
                    }
                }
                cbAcademy.setSelectedIndex(pick);
            }
            loadCoursesForSelectedAcademy();
        } else if (isAcademy()) {
            loadCoursesForAcademyId(academyIdHint);
        }
    }

    private List<AcademyItem> loadAcademies() {
        List<AcademyItem> list = new ArrayList<>();
        final String SQL = "SELECT academy_id, name FROM Academies ORDER BY name ASC";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(new AcademyItem(rs.getInt(1), rs.getString(2)));
        } catch (Exception ex) { ex.printStackTrace(); showError("DB error: " + ex.getMessage()); }
        return list;
    }

    private void loadCoursesForSelectedAcademy() {
        AcademyItem it = (AcademyItem) cbAcademy.getSelectedItem();
        if (it != null) loadCoursesForAcademyId(it.id);
    }

    private void loadCoursesForAcademyId(int academyId) {
        DefaultComboBoxModel<CourseItem> m = new DefaultComboBoxModel<>();
        final String SQL = "SELECT course_id, course_name FROM Courses WHERE academy_id=? ORDER BY course_name ASC";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL)) {
            ps.setInt(1, academyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) m.addElement(new CourseItem(rs.getInt(1), rs.getString(2), academyId));
            }
        } catch (Exception ex) { ex.printStackTrace(); showError("DB error: " + ex.getMessage()); }

        cbCourse.setModel(m);
        if (m.getSize() > 0) {
            cbCourse.setSelectedIndex(0);
            loadSelectedCourseDetails();
        } else {
            clearFields();
        }
    }

    private void loadSelectedCourseDetails() {
        CourseItem item = (CourseItem) cbCourse.getSelectedItem();
        if (item == null) { clearFields(); return; }

        final String SQL = "SELECT course_name, course_image, description, start_date, end_date, price, duration " +
                           "FROM Courses WHERE course_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL)) {
            ps.setInt(1, item.id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tfName.setText(safe(rs.getString(1)));
                    tfImage.setText(safe(rs.getString(2)));
                    taDesc.setText(safe(rs.getString(3)));
                    java.sql.Date sd = rs.getDate(4);
                    java.sql.Date ed = rs.getDate(5);
                    tfStart.setText(sd == null ? "" : sd.toLocalDate().toString());
                    tfEnd.setText  (ed == null ? "" : ed.toLocalDate().toString());
                    tfPrice.setText(rs.getBigDecimal(6) == null ? "" : rs.getBigDecimal(6).toPlainString());
                    tfDuration.setText(safe(rs.getString(7)));
                } else {
                    clearFields();
                }
            }
        } catch (Exception ex) { ex.printStackTrace(); showError("DB error: " + ex.getMessage()); }
    }

    private void clearFields() {
        tfName.setText(""); tfImage.setText(""); taDesc.setText("");
        tfStart.setText(""); tfEnd.setText(""); tfPrice.setText(""); tfDuration.setText("");
    }

    private void doUpdate() {
        CourseItem item = (CourseItem) cbCourse.getSelectedItem();
        if (item == null) { warn("Choose a course first."); return; }

        String name = tfName.getText().trim();
        if (name.isEmpty()) { warn("Name is required."); tfName.requestFocus(); return; }

        String image = emptyToNull(tfImage.getText().trim());
        String desc  = emptyToNull(taDesc.getText().trim());
        java.sql.Date start = parseDate(tfStart.getText().trim());
        java.sql.Date end   = parseDate(tfEnd.getText().trim());
        java.math.BigDecimal price = parsePrice(tfPrice.getText().trim());
        String duration = emptyToNull(tfDuration.getText().trim());

        String SQL = "UPDATE Courses SET course_name=?, course_image=?, description=?, start_date=?, end_date=?, price=?, duration=? " +
                     "WHERE course_id=? " + (isAcademy() ? "AND academy_id=?" : "");
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL)) {
            int i = 1;
            ps.setString(i++, name);
            ps.setString(i++, image);
            ps.setString(i++, desc);
            if (start == null) ps.setNull(i++, Types.DATE); else ps.setDate(i++, start);
            if (end   == null) ps.setNull(i++, Types.DATE); else ps.setDate(i++, end);
            if (price == null) ps.setNull(i++, Types.DECIMAL); else ps.setBigDecimal(i++, price);
            ps.setString(i++, duration);
            ps.setInt(i++, item.id);
            if (isAcademy()) ps.setInt(i++, item.academyId);

            int n = ps.executeUpdate();
            if (n > 0) {
                info("Course updated ✓");
                item.name = name;
                cbCourse.repaint();
            } else {
                warn("Update failed (permission or not found).");
            }
        } catch (Exception ex) { ex.printStackTrace(); showError("DB error: " + ex.getMessage()); }
    }

    private java.sql.Date parseDate(String s) {
        if (s == null || s.isEmpty()) return null;
        try { return java.sql.Date.valueOf(LocalDate.parse(s)); }
        catch (Exception e) { warn("Invalid date: " + s + " (use YYYY-MM-DD)"); return null; }
    }
    private java.math.BigDecimal parsePrice(String s) {
        if (s == null || s.isEmpty()) return null;
        try { return new java.math.BigDecimal(s); }
        catch (Exception e) { warn("Invalid price."); return null; }
    }
    private String emptyToNull(String s) { return (s == null || s.isEmpty()) ? null : s; }
    private static String safe(String s) { return s == null ? "" : s; }

    private void warn(String m)  { JOptionPane.showMessageDialog(this, m, "Warning", JOptionPane.WARNING_MESSAGE); }
    private void info(String m)  { JOptionPane.showMessageDialog(this, m, "Info", JOptionPane.INFORMATION_MESSAGE); }
    private void showError(String m) { JOptionPane.showMessageDialog(this, m, "Error", JOptionPane.ERROR_MESSAGE); }

    private static class AcademyItem {
        final int id; final String name;
        AcademyItem(int id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name + " (ID: " + id + ")"; }
    }
    private static class CourseItem {
        final int id; String name; final int academyId;
        CourseItem(int id, String name, int academyId) { this.id = id; this.name = name; this.academyId = academyId; }
        @Override public String toString() { return name; }
    }
}
