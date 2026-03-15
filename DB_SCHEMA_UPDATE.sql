-- Run this in your SQLite database if tables/columns are missing.
-- tbl_students: for storing student program when type=Student (used by Register)
CREATE TABLE IF NOT EXISTS tbl_students (
  student_id INTEGER PRIMARY KEY AUTOINCREMENT,
  a_id INTEGER NOT NULL REFERENCES tbl_accounts(a_id),
  program TEXT NOT NULL
);

-- Add t_id (teacher who entered the grade) to tbl_grades if not present.
-- Step 1: Add the column
ALTER TABLE tbl_grades ADD COLUMN t_id INTEGER;

-- Step 2: Set t_id for existing rows (replace 1 with your teacher's a_id from tbl_accounts if needed)
UPDATE tbl_grades SET t_id = 1 WHERE t_id IS NULL;

-- See HOW_TO_ADD_T_ID.md for full instructions. Or run Config.AddTIdColumn.java once.

-- Report card requests (student requests, admin generates or denies)
CREATE TABLE IF NOT EXISTS tbl_report_requests (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  a_id INTEGER NOT NULL REFERENCES tbl_accounts(a_id),
  status TEXT NOT NULL DEFAULT 'Pending',
  requested_at TEXT
);
