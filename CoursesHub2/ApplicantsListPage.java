package CoursesHub2;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.*;

public class ApplicantsListPage extends JFrame {
    private JTable table;
    private DefaultTableModel model;

    public ApplicantsListPage() {
        setTitle("Job Applicants");
        setIconImage(new ImageIcon("src/images/bg_logo.png").getImage());
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        Color bgColor = new Color(255, 247, 214);
        Color headerColor = new Color(0, 51, 102);
        Color viewButtonColor = new Color(255, 140, 0); 

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(bgColor);

        // ------- Top controls -------
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.setBackground(bgColor);

        JLabel searchLabel = new JLabel("Search by Academy: ");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        searchLabel.setForeground(headerColor);

        JComboBox<String> searchComboBox = new JComboBox<>();
        searchComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        searchComboBox.setEditable(true);
        searchComboBox.setToolTipText("Select an academy to filter applicants");
        searchComboBox.setBorder(new LineBorder(headerColor, 2));
        searchComboBox.addItem("All");
        populateAcademyDomains(searchComboBox);

        JButton resetBtn = new JButton("Reset");
        resetBtn.setFont(new Font("Segoe UI", Font.BOLD, 20));
        resetBtn.setBackground(headerColor);
        resetBtn.setForeground(Color.WHITE);
        resetBtn.setFocusPainted(false);
        resetBtn.setBorder(new LineBorder(headerColor, 2));
        resetBtn.setPreferredSize(new Dimension(100, 40));

        searchComboBox.addActionListener(e -> {
            String selected = (String) searchComboBox.getSelectedItem();
            if (selected != null && !selected.equalsIgnoreCase("All")) {
                filterApplicants(selected);
            } else {
                loadApplicants();
            }
        });

        // ENTER to filter
        JTextField editorComponent = (JTextField) searchComboBox.getEditor().getEditorComponent();
        editorComponent.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String keyword = editorComponent.getText();
                    if (keyword != null && !keyword.equalsIgnoreCase("All")) {
                        filterApplicants(keyword);
                    } else {
                        loadApplicants();
                    }
                }
            }
        });

        resetBtn.addActionListener(e -> {
            searchComboBox.setSelectedItem("All");
            loadApplicants();
        });

        topPanel.add(searchLabel);
        topPanel.add(searchComboBox);
        topPanel.add(resetBtn);
        panel.add(topPanel, BorderLayout.BEFORE_FIRST_LINE);

        // ------- Title -------
        JLabel titleLabel = new JLabel("Job Applicants", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 40));
        titleLabel.setForeground(headerColor);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
        panel.add(titleLabel, BorderLayout.NORTH);

        // ------- Table -------
        model = new DefaultTableModel() {
            @Override public boolean isCellEditable(int row, int col) { return col == 5; }
        };
        model.setColumnIdentifiers(new Object[]{"Photo", "ID", "First Name", "Last Name", "Domain", "View"});

        table = new JTable(model);
        table.setRowHeight(120);
        table.setFont(new Font("Segoe UI", Font.BOLD, 26)); 
        table.setForeground(new Color(0, 51, 100));

        // Header style
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 26));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setBackground(headerColor);
        ((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer())
                .setHorizontalAlignment(JLabel.CENTER);

        // Photo renderer 
        table.getColumn("Photo").setCellRenderer(new ImageRenderer());
        table.getColumnModel().getColumn(0).setPreferredWidth(130);

        // View button renderer/editor
        table.getColumn("View").setCellRenderer(new ButtonRenderer(viewButtonColor));
        table.getColumn("View").setCellEditor(new ButtonEditor(new JCheckBox(), viewButtonColor));
        table.getColumnModel().getColumn(5).setPreferredWidth(150);

        // Center all text columns (ID, First, Last, Domain)
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        centerRenderer.setVerticalAlignment(JLabel.CENTER);
        for (int i = 1; i < table.getColumnCount() - 1; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        loadApplicants();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(bgColor);
        panel.add(scrollPane, BorderLayout.CENTER);

        // ------- Back -------
        JButton backBtn = new JButton("← Back");
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 26));
        backBtn.setBackground(headerColor);
        backBtn.setForeground(Color.WHITE);
        backBtn.setFocusPainted(false);
        backBtn.setPreferredSize(new Dimension(220, 55));
        backBtn.addActionListener(e -> dispose());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setBackground(bgColor);
        bottomPanel.add(backBtn);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        add(panel);
        setVisible(true);
    }

    private void filterApplicants(String keyword) {
        model.setRowCount(0);
        try (Connection conn = DBConnection.getConnection()) {
            String query =
                "SELECT j.app_id, j.firstname, j.lastname, j.domain, j.photo_path " +
                "FROM JobApplications j JOIN Academies a ON j.academy_id = a.academy_id " +
                "WHERE a.name LIKE ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                addRowFromResult(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error while filtering applicants.");
        }
    }

    private void loadApplicants() {
        model.setRowCount(0);
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT app_id, firstname, lastname, domain, photo_path FROM JobApplications");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                addRowFromResult(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "⚠️ Failed to load applicants.");
        }
    }

    private void addRowFromResult(ResultSet rs) throws SQLException {
        int id = rs.getInt("app_id");
        String fname = rs.getString("firstname");
        String lname = rs.getString("lastname");
        String domain = rs.getString("domain");
        String photoPath = rs.getString("photo_path");

        ImageIcon icon;
        try {
            Image img = new ImageIcon(photoPath).getImage()
                    .getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            icon = new ImageIcon(img);
        } catch (Exception ex) {
            Image fallback = new ImageIcon("src/images/avatar.png").getImage()
                    .getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            icon = new ImageIcon(fallback);
        }
        model.addRow(new Object[]{icon, id, fname, lname, domain, "View"});
    }

    private void populateAcademyDomains(JComboBox<String> comboBox) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT DISTINCT a.name FROM Academies a JOIN JobApplications j ON a.academy_id = j.academy_id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) comboBox.addItem(rs.getString("name"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ---------- Renderers / Editors ----------

    class ImageRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lbl = new JLabel();
            lbl.setHorizontalAlignment(JLabel.CENTER);
            lbl.setVerticalAlignment(JLabel.CENTER);
            lbl.setOpaque(true);
            lbl.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            if (value instanceof ImageIcon) lbl.setIcon((ImageIcon) value);
            return lbl;
        }
    }

    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer(Color color) {
            setOpaque(true);
            setBackground(color);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 20));
        }
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "View" : value.toString());
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        private int selectedRow;

        public ButtonEditor(JCheckBox checkBox, Color color) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.setBackground(color);
            button.setForeground(Color.WHITE);
            button.setFont(new Font("Segoe UI", Font.BOLD, 20));
            button.addActionListener((ActionEvent e) -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable table, Object value, boolean isSelected, int row, int column) {
            label = (value == null) ? "View" : value.toString();
            button.setText(label);
            isPushed = true;
            selectedRow = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                int appId = (int) model.getValueAt(selectedRow, 1);
                new ViewApplicantPage(appId, ApplicantsListPage.this);
            }
            isPushed = false;
            return label;
        }
    }
}
