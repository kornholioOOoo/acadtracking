package Config;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JOptionPane;

/**
 * Run this class once (right-click → Run File) to add the t_id column to tbl_grades
 * and set it for existing rows. After it succeeds, Grades (Allgrades) will display correctly.
 */
public class AddTIdColumn {

    public static void main(String[] args) {
        try (Connection conn = config.connectDB(); Statement st = conn.createStatement()) {

            // 1) Add t_id column if it doesn't exist (SQLite will throw if column exists)
            try {
                st.executeUpdate("ALTER TABLE tbl_grades ADD COLUMN t_id INTEGER");
                System.out.println("Column t_id added to tbl_grades.");
            } catch (SQLException e) {
                if (e.getMessage() != null && e.getMessage().toLowerCase().contains("duplicate column")) {
                    System.out.println("Column t_id already exists.");
                } else {
                    throw e;
                }
            }

            // 2) Set t_id for rows where it is NULL (use 1 as default teacher a_id; change if needed)
            int updated = st.executeUpdate("UPDATE tbl_grades SET t_id = 1 WHERE t_id IS NULL");
            System.out.println("Updated " + updated + " row(s) with t_id = 1.");

            JOptionPane.showMessageDialog(null,
                "t_id column is ready.\n" +
                "Updated " + updated + " existing grade row(s) with t_id = 1.\n" +
                "You can now use the Grades (Allgrades) screen.");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }
}
