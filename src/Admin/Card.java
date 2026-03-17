/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Admin;

import Config.config;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;
import net.proteanit.sql.DbUtils;

/**
 *
 * @author Dell
 */
public class Card extends javax.swing.JFrame {

    private int studentAId;
    private boolean forReport;
    private volatile String savedImagePath;
    private volatile String saveErrorMessage;
    private File reportSaveDirectory;

    /** Creates form for the given student (used when opening from grades Request). */
    public Card(int studentAId) {
        this.studentAId = studentAId;
        this.forReport = false;
        initComponents();
        card.getTableHeader().setVisible(false);
        jScrollPane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        loadStudentInfo();
        loadGradesTable();
    }

    /** Creates form for report generation: loads student, hides Done panel, captures to image when shown. */
    public Card(int studentAId, boolean forReport) {
        this.studentAId = studentAId;
        this.forReport = forReport;
        initComponents();
        card.getTableHeader().setVisible(false);
        jScrollPane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        loadStudentInfo();
        loadGradesTable();
        adjustGradesTableColumns();
        if (forReport) {
            done.setVisible(false);
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowOpened(WindowEvent e) {
                    // Make sure layout is settled before capturing.
                    SwingUtilities.invokeLater(() -> {
                        saveCardToImage();
                        // For report generation mode we don't want to keep this window around.
                        dispose();
                    });
                }
            });
        }
    }

    /** Creates form for report generation and saves into the chosen directory. */
    public Card(int studentAId, boolean forReport, File reportSaveDirectory) {
        this.studentAId = studentAId;
        this.forReport = forReport;
        this.reportSaveDirectory = reportSaveDirectory;
        initComponents();
        card.getTableHeader().setVisible(false);
        jScrollPane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        loadStudentInfo();
        loadGradesTable();
        adjustGradesTableColumns();
        if (forReport) {
            done.setVisible(false);
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowOpened(WindowEvent e) {
                    SwingUtilities.invokeLater(() -> {
                        saveCardToImage();
                        dispose();
                    });
                }
            });
        }
    }

    /** Returns the path where the report card image was saved, or null. */
    public String getSavedImagePath() {
        return savedImagePath;
    }

    /** Returns an error message if saving failed, or null. */
    public String getSaveErrorMessage() {
        return saveErrorMessage;
    }

    private void saveCardToImage() {
        try {
            saveErrorMessage = null;
            File dir = (reportSaveDirectory != null) ? reportSaveDirectory
                    : new File(System.getProperty("user.home") + File.separator + "ReportCards");
            if (!dir.exists() && !dir.mkdirs()) {
                throw new IOException("Unable to create output folder: " + dir.getAbsolutePath());
            }
            String fileName = "ReportCard_" + studentAId + "_" + System.currentTimeMillis() + ".png";
            File file = new File(dir, fileName);

            // Ensure the panel has a non-zero size (can be 0x0 if captured too early).
            int w = jPanel2.getWidth();
            int h = jPanel2.getHeight();
            if (w <= 0 || h <= 0) {
                Dimension pref = jPanel2.getPreferredSize();
                w = Math.max(1, pref.width);
                h = Math.max(1, pref.height);
                jPanel2.setSize(w, h);
                jPanel2.doLayout();
            }

            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            jPanel2.printAll(g2);
            g2.dispose();
            if (!ImageIO.write(img, "png", file)) {
                throw new IOException("No PNG writer available to save image.");
            }
            savedImagePath = file.getAbsolutePath();
        } catch (Exception ex) {
            savedImagePath = null;
            saveErrorMessage = ex.getMessage();
            if (!forReport) {
                JOptionPane.showMessageDialog(this, "Failed to save report card image:\n" + ex.getMessage());
            }
        }
    }

    /** Default constructor (no student). */
    public Card() {
        this.studentAId = 0;
        this.forReport = false;
        initComponents();
        card.getTableHeader().setVisible(false);
        jScrollPane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }

    private void loadStudentInfo() {
        try (Connection conn = config.connectDB()) {
            String sqlAcc = "SELECT fname, lname FROM tbl_accounts WHERE a_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlAcc)) {
                ps.setInt(1, studentAId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        name.setText(rs.getString("fname") + " " + rs.getString("lname"));
                    }
                }
            }
            String sqlSt = "SELECT program FROM tbl_students WHERE a_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlSt)) {
                ps.setInt(1, studentAId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        pANDy.setText(rs.getString("program"));
                    } else {
                        pANDy.setText("—");
                    }
                }
            }
            date.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            String sqlSem = "SELECT s.sem FROM tbl_grades g JOIN tbl_subjects s ON g.s_id = s.s_id WHERE g.a_id = ? LIMIT 1";
            try (PreparedStatement ps = conn.prepareStatement(sqlSem)) {
                ps.setInt(1, studentAId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        sem.setText(rs.getString("sem"));
                    } else {
                        sem.setText("—");
                    }
                }
            }
        } catch (SQLException e) {
            name.setText("—");
            pANDy.setText("—");
            sem.setText("—");
            date.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        }
    }

    private void loadGradesTable() {
        String sql = "SELECT s.s_name, g.prelim, g.midterm, g.prefinal, g.finals, "
            + "CASE WHEN (g.prelim + g.midterm + g.prefinal + g.finals) / 4.0 BETWEEN 1.0 AND 3.0 THEN 'Passed' "
            + "WHEN (g.prelim + g.midterm + g.prefinal + g.finals) / 4.0 BETWEEN 3.1 AND 5.0 THEN 'Failed' ELSE '' END AS remarks "
            + "FROM tbl_grades g JOIN tbl_subjects s ON g.s_id = s.s_id WHERE g.a_id = ? ORDER BY s.s_name";
        try (Connection conn = config.connectDB();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentAId);
            try (ResultSet rs = ps.executeQuery()) {
                card.setModel(DbUtils.resultSetToTableModel(rs));
            }
        } catch (SQLException e) {
            card.setModel(new DefaultTableModel());
        }
        adjustGradesTableColumns();
    }

    private void adjustGradesTableColumns() {
        try {
            // Fit columns to the visible area to avoid a horizontal scrollbar.
            card.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);

            // Expected columns: s_name, prelim, midterm, prefinal, finals, remarks
            if (card.getColumnModel().getColumnCount() >= 6) {
                // Subject name gets the most space.
                card.getColumnModel().getColumn(0).setPreferredWidth(260);

                // Grade columns compact
                card.getColumnModel().getColumn(1).setPreferredWidth(70);
                card.getColumnModel().getColumn(2).setPreferredWidth(70);
                card.getColumnModel().getColumn(3).setPreferredWidth(70);
                card.getColumnModel().getColumn(4).setPreferredWidth(70);

                // Remarks moderate
                card.getColumnModel().getColumn(5).setPreferredWidth(110);
            } else if (card.getColumnModel().getColumnCount() > 0) {
                // Fallback: make first column wider if table shape changes.
                card.getColumnModel().getColumn(0).setPreferredWidth(260);
            }

            card.setRowHeight(22);
        } catch (Exception ignored) {
            // Table may not be ready yet; safe to ignore.
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        date = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        name = new javax.swing.JLabel();
        pANDy = new javax.swing.JLabel();
        sem = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        card = new javax.swing.JTable();
        jPanel9 = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        jPanel11 = new javax.swing.JPanel();
        jLabel19 = new javax.swing.JLabel();
        jPanel12 = new javax.swing.JPanel();
        jLabel20 = new javax.swing.JLabel();
        jPanel13 = new javax.swing.JPanel();
        jLabel21 = new javax.swing.JLabel();
        jPanel14 = new javax.swing.JPanel();
        jLabel22 = new javax.swing.JLabel();
        done = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(0, 153, 153));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/logo.png"))); // NOI18N
        jPanel2.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 130, 230, 220));

        jLabel11.setFont(new java.awt.Font("Arial Black", 1, 12)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(0, 51, 102));
        jLabel11.setText("Smart Tracking for Smarter Learning.");
        jPanel2.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 290, 280, 100));

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jPanel3.add(date, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 60, 260, 20));

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("REPORT CARD");
        jPanel3.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 600, -1));

        jLabel4.setText("Date:");
        jPanel3.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 60, 50, -1));

        jLabel5.setText("Name:");
        jPanel3.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 40, 50, -1));

        jLabel6.setText("Semester:");
        jPanel3.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 40, 80, -1));

        jLabel7.setText("Program & Year:");
        jPanel3.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 60, 120, -1));
        jPanel3.add(name, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 40, 230, 20));
        jPanel3.add(pANDy, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 60, 190, 20));
        jPanel3.add(sem, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 40, 230, 20));

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        card.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(card);

        jPanel4.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 40, 600, 380));

        jPanel9.setBackground(new java.awt.Color(0, 153, 153));
        jPanel9.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel16.setText("Prelim");

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, 66, Short.MAX_VALUE)
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
        );

        jPanel4.add(jPanel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 0, 70, 40));

        jPanel10.setBackground(new java.awt.Color(0, 153, 153));
        jPanel10.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel17.setText("Remarks");

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel17, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 116, Short.MAX_VALUE)
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
        );

        jPanel4.add(jPanel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 0, 120, 40));

        jPanel11.setBackground(new java.awt.Color(0, 153, 153));
        jPanel11.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel19.setText("Final");

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel19, javax.swing.GroupLayout.DEFAULT_SIZE, 66, Short.MAX_VALUE)
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel19, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
        );

        jPanel4.add(jPanel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 0, 70, 40));

        jPanel12.setBackground(new java.awt.Color(0, 153, 153));
        jPanel12.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel20.setText("Midterm");

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel20, javax.swing.GroupLayout.DEFAULT_SIZE, 66, Short.MAX_VALUE)
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel20, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
        );

        jPanel4.add(jPanel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 0, 70, 40));

        jPanel13.setBackground(new java.awt.Color(0, 153, 153));
        jPanel13.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel21.setText("Prefinal");

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel21, javax.swing.GroupLayout.DEFAULT_SIZE, 66, Short.MAX_VALUE)
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel21, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
        );

        jPanel4.add(jPanel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 0, 70, -1));

        jPanel14.setBackground(new java.awt.Color(0, 153, 153));
        jPanel14.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel22.setText("Subject");

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel22, javax.swing.GroupLayout.DEFAULT_SIZE, 196, Short.MAX_VALUE)
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel22, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
        );

        jPanel4.add(jPanel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 200, 40));

        jPanel3.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 90, 600, 420));

        jPanel2.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 10, 620, 520));

        done.setBackground(new java.awt.Color(0, 153, 153));
        done.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        done.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                doneMouseClicked(evt);
            }
        });

        jLabel18.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel18.setText("DONE");

        javax.swing.GroupLayout doneLayout = new javax.swing.GroupLayout(done);
        done.setLayout(doneLayout);
        doneLayout.setHorizontalGroup(
            doneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)
        );
        doneLayout.setVerticalGroup(
            doneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
        );

        jPanel2.add(done, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 460, 160, -1));

        jPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 950, 540));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 973, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 563, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void doneMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_doneMouseClicked
        Report done = new Report();
        done.setVisible(true);
        dispose();
    }//GEN-LAST:event_doneMouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Card.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Card.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Card.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Card.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Card().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable card;
    private javax.swing.JLabel date;
    private javax.swing.JPanel done;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel name;
    private javax.swing.JLabel pANDy;
    private javax.swing.JLabel sem;
    // End of variables declaration//GEN-END:variables
}
