/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Admin;

import Config.config;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
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

    /** Creates form for the given student (used when opening from grades Request). */
    public Card(int studentAId) {
        this.studentAId = studentAId;
        this.forReport = false;
        initComponents();
        card.getTableHeader().setVisible(false);
        loadStudentInfo();
        loadGradesTable();
    }

    /** Creates form for report generation: loads student, hides Done panel, captures to image when shown. */
    public Card(int studentAId, boolean forReport) {
        this.studentAId = studentAId;
        this.forReport = forReport;
        initComponents();
        card.getTableHeader().setVisible(false);
        loadStudentInfo();
        loadGradesTable();
        if (forReport) {
            done.setVisible(false);
            SwingUtilities.invokeLater(() -> {
                javax.swing.Timer t = new javax.swing.Timer(800, e -> {
                    ((javax.swing.Timer) e.getSource()).stop();
                    saveCardToImage();
                });
                t.setRepeats(false);
                t.start();
            });
        }
    }

    /** Returns the path where the report card image was saved, or null. */
    public String getSavedImagePath() {
        return savedImagePath;
    }

    private void saveCardToImage() {
        try {
            String dirPath = System.getProperty("user.home") + File.separator + "ReportCards";
            File dir = new File(dirPath);
            if (!dir.exists()) dir.mkdirs();
            String fileName = "ReportCard_" + studentAId + "_" + System.currentTimeMillis() + ".png";
            File file = new File(dir, fileName);
            BufferedImage img = new BufferedImage(jPanel2.getWidth(), jPanel2.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = img.createGraphics();
            jPanel2.paint(g2);
            g2.dispose();
            ImageIO.write(img, "png", file);
            savedImagePath = file.getAbsolutePath();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /** Default constructor (no student). */
    public Card() {
        this.studentAId = 0;
        this.forReport = false;
        initComponents();
        card.getTableHeader().setVisible(false);
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
        jPanel8 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        card = new javax.swing.JTable();
        jPanel6 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
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

        jPanel8.setBackground(new java.awt.Color(0, 153, 153));
        jPanel8.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel15.setText("Subject");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 8, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
        );

        jPanel4.add(jPanel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 0, 80, 40));

        jPanel5.setBackground(new java.awt.Color(0, 153, 153));
        jPanel5.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel12.setText("Subject");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 40, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
        );

        jPanel4.add(jPanel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(1, 1, -1, 40));

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

        jPanel6.setBackground(new java.awt.Color(0, 153, 153));
        jPanel6.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel13.setText("Subject");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 40, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, 44, Short.MAX_VALUE)
        );

        jPanel4.add(jPanel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 0, -1, -1));

        jPanel7.setBackground(new java.awt.Color(0, 153, 153));
        jPanel7.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel14.setText("Subject");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 32, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
        );

        jPanel4.add(jPanel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 0, -1, -1));

        jPanel9.setBackground(new java.awt.Color(0, 153, 153));
        jPanel9.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel16.setText("Subject");

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                .addGap(0, 8, Short.MAX_VALUE)
                .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
        );

        jPanel4.add(jPanel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 0, 80, 40));

        jPanel10.setBackground(new java.awt.Color(0, 153, 153));
        jPanel10.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel17.setText("Subject");

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
        );

        jPanel4.add(jPanel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(522, 0, 80, 40));

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
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel name;
    private javax.swing.JLabel pANDy;
    private javax.swing.JLabel sem;
    // End of variables declaration//GEN-END:variables
}
