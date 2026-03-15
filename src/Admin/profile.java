/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Admin;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import Config.config;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.SwingUtilities;

public class profile extends javax.swing.JFrame {

    private int userId;
    private JLabel profilePicLabel;
    private String profilePicPath;
    private BufferedImage profilePicOriginal;
    private JButton togglePwBtn;
    private boolean passwordVisible = false;
    private String passwordValue = "";

    // ✅ Constructor: No-arg version now safe
    public profile() {
    // Now no need for login check here
    this.userId = Config.Session.userId;
    initComponents();
    initProfilePictureUI();
    applyTheme();
    displayData();
}



    // ✅ Optional: Constructor with explicit userId
    public profile(int userId) {
        this.userId = userId;
        initComponents();
        initProfilePictureUI();
        applyTheme();
        if (!Config.Session.isLoggedIn) {
            JOptionPane.showMessageDialog(this, "You must login first!");
            new Main.Login().setVisible(true);
            SwingUtilities.invokeLater(() -> dispose());
            return;
        }
        displayData();
    }

    private void applyTheme() {
        // Keep the same palette, just make it feel more "designed"
        Color teal = new Color(0, 153, 153);
        Color lightTeal = new Color(0, 204, 204);
        Color textDark = new Color(0, 51, 51);

        jPanel1.setBackground(lightTeal);
        jPanel2.setBackground(teal);

        // Card-like look for the picture panel
        jPanel3.setBackground(teal);
        jPanel3.setBorder(new CompoundBorder(new LineBorder(new Color(255, 255, 255, 80), 1, true), new EmptyBorder(8, 8, 8, 8)));

        // Improve label typography
        java.awt.Font labelFont = new java.awt.Font("Tahoma", java.awt.Font.BOLD, 18);
        java.awt.Font valueFont = new java.awt.Font("Tahoma", java.awt.Font.BOLD, 14);

        Fname.setFont(labelFont);
        Lname.setFont(labelFont);
        Id.setFont(labelFont);
        Email.setFont(labelFont);
        Utype.setFont(labelFont);
        Pass.setFont(labelFont);

        Fn.setFont(valueFont);
        Ln.setFont(valueFont);
        id.setFont(valueFont);
        Em.setFont(valueFont);
        Ut.setFont(valueFont);
        Pw.setFont(valueFont);

        Fname.setForeground(textDark);
        Lname.setForeground(textDark);
        Id.setForeground(textDark);
        Email.setForeground(textDark);
        Utype.setForeground(textDark);
        Pass.setForeground(textDark);

        Fn.setForeground(textDark);
        Ln.setForeground(textDark);
        id.setForeground(textDark);
        Em.setForeground(textDark);
        Ut.setForeground(textDark);
        Pw.setForeground(textDark);

        // Make interactive panels feel clickable
        styleActionPanel(Edit);
        styleActionPanel(Back);
        styleActionPanel(Logout);

        // Password toggle button polish
        if (togglePwBtn != null) {
            togglePwBtn.setBackground(teal);
            togglePwBtn.setForeground(Color.WHITE);
            togglePwBtn.setBorder(new CompoundBorder(new LineBorder(new Color(255, 255, 255, 120), 1, true), new EmptyBorder(4, 10, 4, 10)));
            togglePwBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            SwingUtilities.invokeLater(this::positionTogglePwButton);
        }

        // Picture label polish (fills panel; keep centered text when no image)
        if (profilePicLabel != null) {
            profilePicLabel.setBackground(lightTeal);
            profilePicLabel.setBorder(new LineBorder(new Color(255, 255, 255, 120), 1, true));
            profilePicLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            profilePicLabel.setText(profilePicLabel.getIcon() == null ? "Click to add photo" : null);
        }

        // Small name text in the lower area should read nicely
        fn.setForeground(Color.BLACK);
        ln.setForeground(Color.BLACK);

        repaint();
    }

    private void styleActionPanel(JPanel panel) {
        if (panel == null) return;
        panel.setBackground(new Color(0, 153, 153));
        panel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        // Add hover effect without requiring form edits
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                panel.setBackground(new Color(0, 204, 204));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                panel.setBackground(new Color(0, 153, 153));
            }
        });
    }

    private void initProfilePictureUI() {
        // Add a profile picture placeholder into jPanel3 without modifying generated code.
        if (profilePicLabel == null) {
            profilePicLabel = new JLabel("No Photo", JLabel.CENTER);
            profilePicLabel.setOpaque(true);
            profilePicLabel.setBackground(new Color(0, 204, 204));
            profilePicLabel.setForeground(Color.WHITE);
            jPanel3.add(profilePicLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, jPanel3.getWidth(), jPanel3.getHeight()));

            // Click the picture area to change it
            profilePicLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    chooseAndSaveProfilePicture();
                }
            });

            // Keep picture fully occupying the panel (and re-scale on resize)
            jPanel3.addComponentListener(new java.awt.event.ComponentAdapter() {
                @Override
                public void componentResized(java.awt.event.ComponentEvent e) {
                    profilePicLabel.setBounds(0, 0, jPanel3.getWidth(), jPanel3.getHeight());
                    refreshProfilePictureScale();
                }
            });
        }

        // Keep the "Edit" feature for editing credentials
        Edit.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                editCredentials();
            }
        });

        initPasswordToggleUI();

        loadProfilePictureFromDisk();
        jPanel3.revalidate();
        jPanel3.repaint();
    }

    private void initPasswordToggleUI() {
        if (togglePwBtn != null) {
            return;
        }

        togglePwBtn = new JButton("Show");
        togglePwBtn.setBackground(new Color(0, 153, 153));
        togglePwBtn.setForeground(Color.WHITE);
        togglePwBtn.setFocusable(false);
        togglePwBtn.setMargin(new java.awt.Insets(2, 8, 2, 8));
        togglePwBtn.addActionListener(e -> togglePasswordVisibility());

        // jPanel4 uses AbsoluteLayout: must add with AbsoluteConstraints (same row as Pw at 209,340)
        int x = 209 + 150 + 10;
        int y = 340;
        jPanel4.add(togglePwBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(x, y, 70, 28));
        jPanel1.revalidate();
        jPanel1.repaint();

        updatePasswordDisplay();
    }

    private void positionTogglePwButton() {
        if (togglePwBtn == null || Pw == null) return;

        java.awt.Dimension pref = togglePwBtn.getPreferredSize();
        int btnW = Math.max(70, pref.width);
        int btnH = Math.max(28, pref.height);

        int gap = 10;
        int x = Pw.getX() + Pw.getWidth() + gap;
        int y = Pw.getY() + Math.max(0, (Pw.getHeight() - btnH) / 2);

        // Keep inside jPanel1 bounds
        int maxX = Math.max(0, jPanel4.getWidth() - btnW - 10);
        if (x > maxX) x = maxX;

        togglePwBtn.setBounds(x, y, btnW, btnH);
        togglePwBtn.revalidate();
        togglePwBtn.repaint();
    }

    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        updatePasswordDisplay();
    }

    private void updatePasswordDisplay() {
        if (Pw == null) return;

        if (passwordValue == null) {
            passwordValue = "";
        }

        if (passwordVisible) {
            Pw.setText(passwordValue);
            if (togglePwBtn != null) togglePwBtn.setText("Hide");
        } else {
            Pw.setText(maskPassword(passwordValue));
            if (togglePwBtn != null) togglePwBtn.setText("Show");
        }
    }

    private String maskPassword(String s) {
        if (s == null || s.isEmpty()) return "";
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) sb.append('*');
        return sb.toString();
    }

    private Path getUserProfilePicturePath(String extension) {
        String safeExt = (extension == null || extension.trim().isEmpty()) ? "png" : extension.toLowerCase();
        return Paths.get("profile_pics", "user_" + userId + "." + safeExt);
    }

    private void loadProfilePictureFromDisk() {
        // Try common extensions
        String[] exts = new String[] { "png", "jpg", "jpeg" };
        for (String ext : exts) {
            Path p = getUserProfilePicturePath(ext);
            if (Files.exists(p)) {
                profilePicPath = p.toString();
                setProfilePictureIcon(new File(profilePicPath));
                return;
            }
        }
    }

    private void chooseAndSaveProfilePicture() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Profile Picture");
        chooser.setFileFilter(new FileNameExtensionFilter("Image Files (png, jpg, jpeg)", "png", "jpg", "jpeg"));

        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selected = chooser.getSelectedFile();
        if (selected == null || !selected.exists()) {
            JOptionPane.showMessageDialog(null, "Invalid file selected.");
            return;
        }

        String name = selected.getName();
        String ext = "png";
        int dot = name.lastIndexOf('.');
        if (dot >= 0 && dot < name.length() - 1) {
            ext = name.substring(dot + 1).toLowerCase();
        }
        if (!(ext.equals("png") || ext.equals("jpg") || ext.equals("jpeg"))) {
            JOptionPane.showMessageDialog(null, "Please select a PNG/JPG image.");
            return;
        }

        try {
            Path dir = Paths.get("profile_pics");
            Files.createDirectories(dir);

            Path dest = getUserProfilePicturePath(ext);
            Files.copy(selected.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
            profilePicPath = dest.toString();

            setProfilePictureIcon(dest.toFile());
            JOptionPane.showMessageDialog(null, "Profile picture updated!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Failed to save profile picture: " + e.getMessage());
        }
    }

    private void setProfilePictureIcon(File imageFile) {
        try {
            if (profilePicLabel == null) return;

            // Validate image by reading it
            BufferedImage img = ImageIO.read(imageFile);
            if (img == null) {
                JOptionPane.showMessageDialog(null, "Selected file is not a valid image.");
                return;
            }

            profilePicOriginal = img;
            int w = profilePicLabel.getWidth() > 0 ? profilePicLabel.getWidth() : jPanel3.getWidth();
            int h = profilePicLabel.getHeight() > 0 ? profilePicLabel.getHeight() : jPanel3.getHeight();
            if (w <= 0) w = 330;
            if (h <= 0) h = 270;
            Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
            profilePicLabel.setText(null);
            profilePicLabel.setIcon(new ImageIcon(scaled));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Failed to load image: " + e.getMessage());
        }
    }

    private void refreshProfilePictureScale() {
        if (profilePicLabel == null || profilePicOriginal == null) return;
        int w = profilePicLabel.getWidth();
        int h = profilePicLabel.getHeight();
        if (w <= 0 || h <= 0) return;
        Image scaled = profilePicOriginal.getScaledInstance(w, h, Image.SCALE_SMOOTH);
        profilePicLabel.setText(null);
        profilePicLabel.setIcon(new ImageIcon(scaled));
    }

    private boolean emailAlreadyInUseByOther(String mail) {
        String normalized = mail == null ? "" : mail.trim().toLowerCase();
        if (normalized.isEmpty()) return false;

        String sql = "SELECT 1 FROM tbl_accounts WHERE lower(email) = ? AND a_id <> ? LIMIT 1";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = config.connectDB();
            ps = conn.prepareStatement(sql);
            ps.setString(1, normalized);
            ps.setInt(2, userId);
            rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Unable to validate email right now. Please try again.");
            return true;
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ignored) {
            }
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ignored) {
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }

    private void editCredentials() {
        javax.swing.JTextField first = new javax.swing.JTextField(Fn.getText());
        javax.swing.JTextField last = new javax.swing.JTextField(Ln.getText());
        javax.swing.JTextField mail = new javax.swing.JTextField(Em.getText());
        javax.swing.JPasswordField passField = new javax.swing.JPasswordField(passwordValue);

        javax.swing.JPanel p = new javax.swing.JPanel(new java.awt.GridLayout(0, 1, 8, 8));
        p.add(new javax.swing.JLabel("First name"));
        p.add(first);
        p.add(new javax.swing.JLabel("Last name"));
        p.add(last);
        p.add(new javax.swing.JLabel("Email"));
        p.add(mail);
        p.add(new javax.swing.JLabel("Password"));
        p.add(passField);

        int choice = JOptionPane.showConfirmDialog(this, p, "Edit Profile", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) return;

        String newF = first.getText().trim();
        String newL = last.getText().trim();
        String newE = mail.getText().trim();
        String newP = new String(passField.getPassword()).trim();

        if (newF.isEmpty() || newL.isEmpty() || newE.isEmpty() || newP.isEmpty()) {
            JOptionPane.showMessageDialog(null, "All fields are required!");
            return;
        }

        if (emailAlreadyInUseByOther(newE)) {
            JOptionPane.showMessageDialog(null, "Email is already in use. Please choose another email.");
            return;
        }

        String sql = "UPDATE tbl_accounts SET fname = ?, lname = ?, email = ?, pass = ? WHERE a_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = config.connectDB();
            ps = conn.prepareStatement(sql);
            ps.setString(1, newF);
            ps.setString(2, newL);
            ps.setString(3, newE);
            ps.setString(4, newP);
            ps.setInt(5, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Failed to update profile: " + e.getMessage());
            return;
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ignored) {
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ignored) {
            }
        }

        displayData();
        JOptionPane.showMessageDialog(null, "Profile updated!");
    }

    // Display user data in labels
    private void displayData() {
        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            con = config.connectDB();
            String sql = "SELECT fname, lname, a_id, email, type, pass FROM tbl_accounts WHERE a_id = ?";
            pst = con.prepareStatement(sql);
            pst.setInt(1, userId);

            rs = pst.executeQuery();
            if (rs.next()) {
                Fn.setText(rs.getString("fname"));
                Ln.setText(rs.getString("lname"));
                id.setText(rs.getString("a_id"));
                Em.setText(rs.getString("email"));
                Ut.setText(rs.getString("type"));
                passwordValue = rs.getString("pass");
                updatePasswordDisplay();
                fn.setText(rs.getString("fname"));
                ln.setText(rs.getString("lname"));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Failed to load profile: " + e.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ignored) {
            }
            try {
                if (pst != null) {
                    pst.close();
                }
            } catch (SQLException ignored) {
            }
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }
    
    private void logout() {
    // 1️⃣ Ask for confirmation
    int choice = JOptionPane.showConfirmDialog(
        this, 
        "Are you sure you want to logout?", 
        "Logout Confirmation", 
        JOptionPane.YES_NO_OPTION, 
        JOptionPane.QUESTION_MESSAGE
    );

    // 2️⃣ If user clicks YES, proceed
    if (choice == JOptionPane.YES_OPTION) {
        // Clear session
        Config.Session.isLoggedIn = false;
        Config.Session.userId = 0; // or -1 if preferred

        // Show login form
        new Main.Login().setVisible(true);

        // Close the current frame
        this.dispose();
    }
    // If NO, do nothing
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
        jLabel1 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        Back = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        Edit = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        Logout = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        fn = new javax.swing.JLabel();
        ln = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        Pw = new javax.swing.JLabel();
        Pass = new javax.swing.JLabel();
        Ut = new javax.swing.JLabel();
        Utype = new javax.swing.JLabel();
        Email = new javax.swing.JLabel();
        Em = new javax.swing.JLabel();
        id = new javax.swing.JLabel();
        Id = new javax.swing.JLabel();
        Ln = new javax.swing.JLabel();
        Lname = new javax.swing.JLabel();
        Fname = new javax.swing.JLabel();
        Fn = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(0, 204, 204));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel2.setBackground(new java.awt.Color(0, 153, 153));
        jPanel2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel1.setFont(new java.awt.Font("Arial Black", 0, 48)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("PROFILE");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(409, 409, 409)
                .addComponent(jLabel1)
                .addContainerGap(427, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1070, 120));

        jPanel3.setBackground(new java.awt.Color(0, 153, 153));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jPanel1.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 150, 330, 270));

        Back.setBackground(new java.awt.Color(0, 153, 153));
        Back.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        Back.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                BackMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                BackMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                BackMouseExited(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("BACK");

        javax.swing.GroupLayout BackLayout = new javax.swing.GroupLayout(Back);
        Back.setLayout(BackLayout);
        BackLayout.setHorizontalGroup(
            BackLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, BackLayout.createSequentialGroup()
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        BackLayout.setVerticalGroup(
            BackLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(BackLayout.createSequentialGroup()
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jPanel1.add(Back, new org.netbeans.lib.awtextra.AbsoluteConstraints(860, 460, 150, 40));

        Edit.setBackground(new java.awt.Color(0, 153, 153));
        Edit.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        Edit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                EditMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                EditMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                EditMouseExited(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("EDIT");

        javax.swing.GroupLayout EditLayout = new javax.swing.GroupLayout(Edit);
        Edit.setLayout(EditLayout);
        EditLayout.setHorizontalGroup(
            EditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, EditLayout.createSequentialGroup()
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        EditLayout.setVerticalGroup(
            EditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
        );

        jPanel1.add(Edit, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 460, 150, 40));

        Logout.setBackground(new java.awt.Color(0, 153, 153));
        Logout.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        Logout.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                LogoutMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                LogoutMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                LogoutMouseExited(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("LOGOUT");

        javax.swing.GroupLayout LogoutLayout = new javax.swing.GroupLayout(Logout);
        Logout.setLayout(LogoutLayout);
        LogoutLayout.setHorizontalGroup(
            LogoutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 326, Short.MAX_VALUE)
        );
        LogoutLayout.setVerticalGroup(
            LogoutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, LogoutLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel1.add(Logout, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 510, 330, -1));

        fn.setBackground(new java.awt.Color(0, 0, 0));
        fn.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        fn.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jPanel1.add(fn, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 430, 130, 26));

        ln.setBackground(new java.awt.Color(0, 0, 0));
        ln.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jPanel1.add(ln, new org.netbeans.lib.awtextra.AbsoluteConstraints(850, 430, 130, 26));

        jPanel4.setBackground(new java.awt.Color(0, 153, 153));
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        Pw.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jPanel4.add(Pw, new org.netbeans.lib.awtextra.AbsoluteConstraints(209, 340, 150, 20));

        Pass.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        Pass.setText("Password:");
        jPanel4.add(Pass, new org.netbeans.lib.awtextra.AbsoluteConstraints(21, 338, -1, -1));

        Ut.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jPanel4.add(Ut, new org.netbeans.lib.awtextra.AbsoluteConstraints(209, 287, 150, 20));

        Utype.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        Utype.setText("User type:");
        jPanel4.add(Utype, new org.netbeans.lib.awtextra.AbsoluteConstraints(21, 280, -1, -1));

        Email.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        Email.setText("Email:");
        jPanel4.add(Email, new org.netbeans.lib.awtextra.AbsoluteConstraints(21, 224, -1, -1));

        Em.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jPanel4.add(Em, new org.netbeans.lib.awtextra.AbsoluteConstraints(209, 226, 150, 20));

        id.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jPanel4.add(id, new org.netbeans.lib.awtextra.AbsoluteConstraints(209, 167, 150, 20));

        Id.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        Id.setText("ID:");
        jPanel4.add(Id, new org.netbeans.lib.awtextra.AbsoluteConstraints(21, 167, -1, -1));

        Ln.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jPanel4.add(Ln, new org.netbeans.lib.awtextra.AbsoluteConstraints(209, 112, 150, 20));

        Lname.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        Lname.setText("Last Name:");
        jPanel4.add(Lname, new org.netbeans.lib.awtextra.AbsoluteConstraints(21, 110, -1, -1));

        Fname.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        Fname.setText("First Name:");
        jPanel4.add(Fname, new org.netbeans.lib.awtextra.AbsoluteConstraints(21, 52, -1, -1));

        Fn.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jPanel4.add(Fn, new org.netbeans.lib.awtextra.AbsoluteConstraints(209, 52, 150, 23));

        jPanel1.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 150, 570, 400));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 584, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void BackMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_BackMouseClicked
        if (Ut.getText().equalsIgnoreCase("Admin")) {
        new adminDashboardOrig().setVisible(true);
    } else if(Ut.getText().equalsIgnoreCase("Student")) {
        new studentDashboard().setVisible(true);
    } else if(Ut.getText().equalsIgnoreCase("Teacher")) {
        new adminDashboard().setVisible(true);
    }
    dispose();
    }//GEN-LAST:event_BackMouseClicked

    private void LogoutMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_LogoutMouseClicked
        logout();
    }//GEN-LAST:event_LogoutMouseClicked

    private void EditMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_EditMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_EditMouseClicked

    private void EditMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_EditMouseEntered
        setColor(Edit);
    }//GEN-LAST:event_EditMouseEntered

    private void EditMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_EditMouseExited
        resetColor(Edit);
    }//GEN-LAST:event_EditMouseExited

    private void BackMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_BackMouseEntered
        setColor(Back);
    }//GEN-LAST:event_BackMouseEntered

    private void BackMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_BackMouseExited
        resetColor(Back);
    }//GEN-LAST:event_BackMouseExited

    private void LogoutMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_LogoutMouseEntered
        setColor(Logout);
    }//GEN-LAST:event_LogoutMouseEntered

    private void LogoutMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_LogoutMouseExited
        resetColor(Logout);
    }//GEN-LAST:event_LogoutMouseExited
    public void setColor(JPanel p){
        p.setBackground(new Color(0, 204, 204));
    }
    
    public void resetColor(JPanel p2){
        p2.setBackground(new Color(0, 153, 153));
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
    java.awt.EventQueue.invokeLater(() -> {
        if (!Config.Session.isLoggedIn) {
            // Show the login-required notification BEFORE opening profile
            JOptionPane.showMessageDialog(null, "You must login first!");
            new Main.Login().setVisible(true); // open login form
        } else {
            new profile().setVisible(true); // open profile normally
        }
    });
}


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel Back;
    private javax.swing.JPanel Edit;
    private javax.swing.JLabel Em;
    private javax.swing.JLabel Email;
    private javax.swing.JLabel Fn;
    private javax.swing.JLabel Fname;
    private javax.swing.JLabel Id;
    private javax.swing.JLabel Ln;
    private javax.swing.JLabel Lname;
    private javax.swing.JPanel Logout;
    private javax.swing.JLabel Pass;
    private javax.swing.JLabel Pw;
    private javax.swing.JLabel Ut;
    private javax.swing.JLabel Utype;
    private javax.swing.JLabel fn;
    private javax.swing.JLabel id;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JLabel ln;
    // End of variables declaration//GEN-END:variables

    private void loadUserData() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
