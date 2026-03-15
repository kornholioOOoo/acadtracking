# How to Create the `t_id` Column in tbl_grades

The **t_id** column stores which teacher entered each grade. Without it, the teacher’s Grades screen (Allgrades) cannot filter grades and will show no data or throw errors.

---

## Why it’s needed

- **Allgrades.java** runs:  
  `SELECT ... FROM tbl_grades g ... WHERE g.t_id = ?`  
  If `t_id` does not exist, the query fails and the table stays empty.
- **addG.java** inserts new grades with `t_id = Session.userId` so future grades have a teacher.

---

## Option A: Using SQLite command line

1. **Locate your database file**  
   It’s usually in the project folder, e.g. `acadtrackDB.db`.

2. **Open a terminal in that folder** and start SQLite:
   ```text
   sqlite3 acadtrackDB.db
   ```

3. **Check if `t_id` already exists:**
   ```sql
   PRAGMA table_info(tbl_grades);
   ```
   Look for a row where the name is `t_id`. If it’s there, you’re done.

4. **Add the column if it’s missing:**
   ```sql
   ALTER TABLE tbl_grades ADD COLUMN t_id INTEGER;
   ```

5. **Set `t_id` for existing rows** (so old grades show under a teacher):  
   Replace `1` with the real `a_id` of a teacher in `tbl_accounts` if you prefer.
   ```sql
   UPDATE tbl_grades SET t_id = 1 WHERE t_id IS NULL;
   ```

6. **Exit SQLite:**
   ```text
   .quit
   ```

---

## Option B: Using a SQL script in DB Browser / DBeaver / etc.

1. Open `acadtrackDB.db` in your SQLite GUI.
2. Open a “Execute SQL” or “Run SQL” window.
3. Run:
   ```sql
   -- Add column (safe to run once; will error if column already exists)
   ALTER TABLE tbl_grades ADD COLUMN t_id INTEGER;

   -- Assign existing grades to teacher with a_id = 1 (change 1 if needed)
   UPDATE tbl_grades SET t_id = 1 WHERE t_id IS NULL;
   ```
4. If you get “duplicate column name: t_id”, the column already exists; run only the `UPDATE` line.

---

## Option C: Run the Java migration once

1. In your project, run the main method of **AddTIdColumn** (see below).
2. It will:
   - Add `t_id` to `tbl_grades` if it’s missing.
   - Set `t_id = 1` for all rows where `t_id` is NULL.
3. After it succeeds, you can remove or ignore that class.

---

## After adding `t_id`

- Restart the app and log in as a **teacher**.
- Open **Grades (Allgrades)**. You should see only that teacher’s grades.
- When teachers add new grades from **addG**, `t_id` is set automatically.

---

## If your table uses `final` instead of `finals`

Some databases use a column named **final** instead of **finals**. If you get errors like “no such column: finals”:

1. Check the column name:
   ```sql
   PRAGMA table_info(tbl_grades);
   ```
2. If you see `final` and not `finals`, either:
   - Rename it in SQLite (3.25.0+):
     ```sql
     ALTER TABLE tbl_grades RENAME COLUMN final TO finals;
     ```
   - Or change the Java code (Allgrades, addG, grades) to use `final` instead of `finals` in every SQL string.
