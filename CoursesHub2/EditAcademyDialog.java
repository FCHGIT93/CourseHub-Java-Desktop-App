package CoursesHub2;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.security.MessageDigest;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EditAcademyDialog extends JDialog {

    private static final Color BLUE    = new Color(0,51,100);
    private static final Color ORANGE  = new Color(255,140,0);
    private static final Color BG      = new Color(255,247,214);  
    private static final Color CARD_BG = new Color(255,244,200);  
    private static final Color BORDER  = new Color(255,170,70);   
    private static final Font  TITLE   = new Font("Segoe UI", Font.BOLD, 32);
    private static final Font  LABEL   = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font  INPUT   = new Font("Segoe UI", Font.PLAIN, 20);

    // UI
    private JComboBox<AcademyItem> cbAcademy;
    private JTextField tfName, tfEmail, tfImageUrl, tfRegLink;
    private JPanel emailRow;

    private JComboBox<UserItem> cbUser;
    private JTextField tfUsername;
    private JPasswordField pfPassword;

    private final Runnable onSaved;
    private boolean academyEmailSupported = false;

    public EditAcademyDialog(Frame owner) { this(owner, null); }

    public EditAcademyDialog(Frame owner, Runnable onSavedOrNull) {
        super(owner, "Edit Academy", true);
        this.onSaved = onSavedOrNull;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setIconImage(new ImageIcon("src/images/bg_logo.png").getImage());
        setResizable(true);

        // Full screen
        Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(0, 0, scr.width, scr.height);

        academyEmailSupported = hasColumn("Academies", "email");

        setContentPane(buildUI());
        installShortcuts();

        // Load data
        List<AcademyItem> items = loadAcademies();
        DefaultComboBoxModel<AcademyItem> model = new DefaultComboBoxModel<>();
        for (AcademyItem it : items) model.addElement(it);
        cbAcademy.setModel(model);

        if (model.getSize() > 0) {
            cbAcademy.setSelectedIndex(0);
            loadSelectedAcademyDetails();
            loadAcademyUsers();
        }
    }

    private JPanel buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);

        JLabel title = new JLabel("Edit Academy", SwingConstants.CENTER);
        title.setFont(TITLE);
        title.setForeground(BLUE);
        title.setBorder(new EmptyBorder(20,20,8,20));
        root.add(title, BorderLayout.NORTH);

        // ===== Card =====
        JPanel card = new JPanel(new GridBagLayout());
        card.setOpaque(true);
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 3, true),
                new EmptyBorder(22,26,22,26)
        ));
        card.setPreferredSize(new Dimension(1100, 620));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(12,14,12,14);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill   = GridBagConstraints.HORIZONTAL;
        gc.gridx=0; gc.gridy=0; gc.weightx=0;

        // Row 0: Academy selector
        card.add(makeLabel("Academy:"), gc);
        gc.gridx=1; gc.weightx=1;
        cbAcademy = new JComboBox<>();
        stylizeInput(cbAcademy);
        cbAcademy.setPreferredSize(new Dimension(600, 46));
        cbAcademy.addActionListener(e -> { loadSelectedAcademyDetails(); loadAcademyUsers(); });
        card.add(cbAcademy, gc);

        // Row 1: Name
        gc.gridx=0; gc.gridy++; gc.weightx=0;
        card.add(makeLabel("Name:"), gc);
        gc.gridx=1; gc.weightx=1;
        tfName = new JTextField();
        stylizeInput(tfName);
        tfName.setPreferredSize(new Dimension(600, 46));
        card.add(tfName, gc);

        // Row 2: Email (optional)
        gc.gridx=0; gc.gridy++; gc.weightx=0;
        emailRow = new JPanel(new BorderLayout(12,0));
        emailRow.setOpaque(false);
        JLabel lbEmail = makeLabel("Email:");
        tfEmail = new JTextField();
        stylizeInput(tfEmail);
        tfEmail.setPreferredSize(new Dimension(600, 46));
        emailRow.add(lbEmail, BorderLayout.WEST);
        emailRow.add(tfEmail, BorderLayout.CENTER);
        if (!academyEmailSupported) emailRow.setVisible(false);
        GridBagConstraints egc = (GridBagConstraints) gc.clone();
        egc.gridwidth = 2; egc.gridx = 0; egc.weightx = 1;
        card.add(emailRow, egc);

        // Row 3: Image URL
        gc.gridwidth=1; gc.gridx=0; gc.gridy++; gc.weightx=0;
        card.add(makeLabel("Photo (image_url):"), gc);
        gc.gridx=1; gc.weightx=1;
        tfImageUrl = new JTextField();
        stylizeInput(tfImageUrl);
        tfImageUrl.setPreferredSize(new Dimension(600, 46));
        card.add(tfImageUrl, gc);

        // Row 4: Registration Link
        gc.gridx=0; gc.gridy++; gc.weightx=0;
        card.add(makeLabel("Registration Link:"), gc);
        gc.gridx=1; gc.weightx=1;
        tfRegLink = new JTextField();
        stylizeInput(tfRegLink);
        tfRegLink.setPreferredSize(new Dimension(600, 46));
        card.add(tfRegLink, gc);

        // Row 5: User combo
        gc.gridx=0; gc.gridy++; gc.weightx=0;
        card.add(makeLabel("Academy Account:"), gc);
        gc.gridx=1; gc.weightx=1;
        cbUser = new JComboBox<>();
        stylizeInput(cbUser);
        cbUser.setPreferredSize(new Dimension(600, 46));
        cbUser.addActionListener(e -> loadSelectedUserDetails());
        card.add(cbUser, gc);

        // Row 6: Username
        gc.gridx=0; gc.gridy++; gc.weightx=0;
        card.add(makeLabel("Username:"), gc);
        gc.gridx=1; gc.weightx=1;
        tfUsername = new JTextField();
        stylizeInput(tfUsername);
        tfUsername.setPreferredSize(new Dimension(600, 46));
        card.add(tfUsername, gc);

        // Row 7: New Password (optional)
        gc.gridx=0; gc.gridy++; gc.weightx=0;
        card.add(makeLabel("New Password (optional):"), gc);
        gc.gridx=1; gc.weightx=1;
        pfPassword = new JPasswordField();
        stylizeInput(pfPassword);
        pfPassword.setPreferredSize(new Dimension(600, 46));
        card.add(pfPassword, gc);

        // ===== Buttons =====
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 16));
        actions.setOpaque(false);

        JButton btnCancel = makeHollowButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        JButton btnUpdate = makeSolidButton("Update");
        btnUpdate.addActionListener(e -> doUpdate());
        getRootPane().setDefaultButton(btnUpdate);

        actions.add(btnCancel);
        actions.add(btnUpdate);

        // ===== Centering + Scroll =====
        JPanel centerColumn = new JPanel(new GridBagLayout());
        centerColumn.setOpaque(false);
        GridBagConstraints wgc = new GridBagConstraints();
        wgc.insets = new Insets(10,10,10,10);
        wgc.gridx=0; wgc.gridy=0; wgc.weightx=1; wgc.weighty=0;
        wgc.fill=GridBagConstraints.NONE; wgc.anchor=GridBagConstraints.CENTER;
        centerColumn.add(card, wgc);
        wgc.gridy=1; centerColumn.add(actions, wgc);

        JPanel centerLayer = new JPanel(new GridBagLayout());
        centerLayer.setBackground(BG);
        GridBagConstraints cg = new GridBagConstraints();
        cg.gridx=0; cg.gridy=0; cg.weightx=1; cg.weighty=1;
        cg.fill=GridBagConstraints.BOTH; cg.anchor=GridBagConstraints.CENTER;
        centerLayer.add(centerColumn, cg);

        JScrollPane scroller = new JScrollPane(centerLayer,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroller.getVerticalScrollBar().setUnitIncrement(24);
        scroller.setBorder(null);

        root.add(scroller, BorderLayout.CENTER);
        return root;
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
                new EmptyBorder(10, 12, 10, 12)
        ));
    }
    private JButton makeSolidButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 20));
        b.setBackground(ORANGE);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(10,18,10,18));
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

    // ==== Loaders / Update / Helpers 
    private void loadSelectedAcademyDetails() {
        AcademyItem it = (AcademyItem) cbAcademy.getSelectedItem();
        if (it == null) return;

        String SQL = "SELECT name, image_url, registration_link"
                   + (academyEmailSupported ? ", email" : "")
                   + " FROM Academies WHERE academy_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, it.id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tfName.setText(safe(rs.getString("name")));
                    tfImageUrl.setText(safe(rs.getString("image_url")));
                    tfRegLink.setText(safe(rs.getString("registration_link")));
                    if (academyEmailSupported) tfEmail.setText(safe(rs.getString("email")));
                }
            }
        } catch (Exception ex) { ex.printStackTrace(); error("DB error: " + ex.getMessage()); }
    }

    private void loadAcademyUsers() {
        cbUser.removeAllItems();
        tfUsername.setText("");
        pfPassword.setText("");

        AcademyItem it = (AcademyItem) cbAcademy.getSelectedItem();
        if (it == null) return;

        List<UserItem> users = new ArrayList<>();
        final String SQL = "SELECT user_id, username FROM Users WHERE role='academy' AND academy_id=? ORDER BY user_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL)) {
            ps.setInt(1, it.id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) users.add(new UserItem(rs.getInt(1), rs.getString(2)));
            }
        } catch (Exception ex) { ex.printStackTrace(); error("DB error: " + ex.getMessage()); }

        if (users.isEmpty()) {
            cbUser.addItem(new UserItem(-1, "<no academy user>"));
            cbUser.setEnabled(false); tfUsername.setEnabled(false); pfPassword.setEnabled(false);
        } else {
            cbUser.setEnabled(true); tfUsername.setEnabled(true); pfPassword.setEnabled(true);
            for (UserItem u : users) cbUser.addItem(u);
            cbUser.setSelectedIndex(0);
            loadSelectedUserDetails();
        }
    }

    private void loadSelectedUserDetails() {
        UserItem u = (UserItem) cbUser.getSelectedItem();
        if (u == null || u.id <= 0) { tfUsername.setText(""); pfPassword.setText(""); return; }
        tfUsername.setText(safe(u.username));
        pfPassword.setText("");
    }

    private List<AcademyItem> loadAcademies() {
        List<AcademyItem> list = new ArrayList<>();
        final String SQL = "SELECT academy_id, name FROM Academies ORDER BY name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(new AcademyItem(rs.getInt(1), rs.getString(2)));
        } catch (Exception ex) { ex.printStackTrace(); error("DB error: " + ex.getMessage()); }
        return list;
    }

    private void doUpdate() {
        AcademyItem it = (AcademyItem) cbAcademy.getSelectedItem();
        if (it == null) { warn("Choose an academy first."); return; }

        String name   = tfName.getText().trim();
        String imgUrl = tfImageUrl.getText().trim();
        String regLnk = tfRegLink.getText().trim();
        String email  = academyEmailSupported ? tfEmail.getText().trim() : null;

        if (name.isEmpty()) { warn("Name is required."); tfName.requestFocus(); return; }

        UserItem selectedUser = (UserItem) cbUser.getSelectedItem();
        boolean hasUser = (selectedUser != null && selectedUser.id > 0);
        String newUsername = hasUser ? tfUsername.getText().trim() : null;
        char[] pwChars     = hasUser ? pfPassword.getPassword() : new char[0];
        String newPassword = new String(pwChars).trim();

        try (Connection conn = DBConnection.getConnection()) {
            boolean oldAuto = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                String SQLA = "UPDATE Academies SET name=?, image_url=?, registration_link=?"
                            + (academyEmailSupported ? ", email=?" : "")
                            + " WHERE academy_id=?";
                try (PreparedStatement ps = conn.prepareStatement(SQLA)) {
                    int idx = 1;
                    ps.setString(idx++, name);
                    ps.setString(idx++, imgUrl.isEmpty()? null : imgUrl);
                    ps.setString(idx++, regLnk.isEmpty()? null : regLnk);
                    if (academyEmailSupported) ps.setString(idx++, email == null || email.isEmpty()? null : email);
                    ps.setInt(idx++, it.id);
                    ps.executeUpdate();
                }

                if (hasUser) {
                    if (newUsername.isEmpty()) throw new SQLException("Username cannot be empty.");
                    if (newPassword.isEmpty()) {
                        String SQLU = "UPDATE Users SET username=? WHERE user_id=? AND role='academy'";
                        try (PreparedStatement ps = conn.prepareStatement(SQLU)) {
                            ps.setString(1, newUsername);
                            ps.setInt(2, selectedUser.id);
                            ps.executeUpdate();
                        }
                    } else {
                        String SQLU = "UPDATE Users SET username=?, password_hash=? WHERE user_id=? AND role='academy'";
                        try (PreparedStatement ps = conn.prepareStatement(SQLU)) {
                            ps.setString(1, newUsername);
                            ps.setString(2, md5HexUpper(newPassword));
                            ps.setInt(3, selectedUser.id);
                            ps.executeUpdate();
                        }
                    }
                }

                conn.commit();
                info("Updated ✓");
                if (onSaved != null) onSaved.run();
                it.name = name; cbAcademy.repaint();
                if (hasUser) { selectedUser.username = newUsername; cbUser.repaint(); }

            } catch (Exception ex) {
                try { conn.rollback(); } catch (Exception ignore) {}
                throw ex;
            } finally {
                try { conn.setAutoCommit(oldAuto); } catch (Exception ignore) {}
            }

        } catch (SQLIntegrityConstraintViolationException dup) {
            warn("Username already exists. Please choose another one.");
        } catch (Exception ex) {
            ex.printStackTrace();
            error("DB error: " + ex.getMessage());
        } finally {
            if (pwChars != null) java.util.Arrays.fill(pwChars, '\0');
        }
    }

    private boolean hasColumn(String table, String column) {
        try (Connection conn = DBConnection.getConnection()) {
            DatabaseMetaData md = conn.getMetaData();
            try (ResultSet rs = md.getColumns(null, null, table, column)) { if (rs.next()) return true; }
            try (ResultSet rs = md.getColumns(null, null, table, column.toUpperCase())) { if (rs.next()) return true; }
            try (ResultSet rs = md.getColumns(null, null, table, column.toLowerCase())) { if (rs.next()) return true; }
        } catch (Exception ignore) {}
        return false;
    }

    private void warn(String m)  { JOptionPane.showMessageDialog(this, m, "Warning", JOptionPane.WARNING_MESSAGE); }
    private void info(String m)  { JOptionPane.showMessageDialog(this, m, "Info", JOptionPane.INFORMATION_MESSAGE); }
    private void error(String m) { JOptionPane.showMessageDialog(this, m, "Error", JOptionPane.ERROR_MESSAGE); }
    private static String safe(String s) { return s == null ? "" : s; }

    private static String md5HexUpper(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] dig = md.digest(text.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder(dig.length * 2);
            for (byte b : dig) sb.append(String.format("%02X", b));
            return sb.toString();
        } catch (Exception e) { throw new RuntimeException("MD5 error", e); }
    }

    private static class AcademyItem {
        final int id; String name;
        AcademyItem(int id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name + "  (ID: " + id + ")"; }
    }
    private static class UserItem {
        final int id; String username;
        UserItem(int id, String username) { this.id = id; this.username = username; }
        @Override public String toString() { return username; }
    }
}
