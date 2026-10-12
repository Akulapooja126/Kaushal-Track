-- OPTIONAL sample data so the dashboard is not empty. Run AFTER schema_v2.sql.
-- Delete these trainees from the website when you want a clean database.

INSERT INTO courses (name, sector) VALUES
  ('Full Stack Development', 'IT'),
  ('Data Analytics', 'IT'),
  ('Electrician', 'Technical'),
  ('Retail Sales', 'Services'),
  ('Tailoring', 'Self-employment')
ON CONFLICT (name) DO NOTHING;

INSERT INTO trainees (full_name, email, phone, gender, district, course_id, enrolled_on, status, attendance_pct, assessment_score) VALUES
  ('Asha Reddy',   'asha@example.com',   '9000000001', 'Female', 'Hyderabad',  (SELECT id FROM courses WHERE name = 'Full Stack Development'), '2026-03-01', 'COMPLETED',   92, 88),
  ('Ravi Kumar',   'ravi@example.com',   '9000000002', 'Male',   'Warangal',   (SELECT id FROM courses WHERE name = 'Full Stack Development'), '2026-03-01', 'COMPLETED',   81, 74),
  ('Sneha Patel',  'sneha@example.com',  '9000000003', 'Female', 'Hyderabad',  (SELECT id FROM courses WHERE name = 'Data Analytics'),         '2026-03-01', 'COMPLETED',   88, 91),
  ('Imran Khan',   'imran@example.com',  '9000000004', 'Male',   'Nizamabad',  (SELECT id FROM courses WHERE name = 'Electrician'),            '2026-02-10', 'COMPLETED',   78, 70),
  ('Lakshmi Devi', 'lakshmi@example.com','9000000005', 'Female', 'Karimnagar', (SELECT id FROM courses WHERE name = 'Tailoring'),               '2026-02-10', 'COMPLETED',   85, 80),
  ('Suresh Babu',  'suresh@example.com', '9000000006', 'Male',   'Khammam',    (SELECT id FROM courses WHERE name = 'Retail Sales'),           '2026-02-10', 'COMPLETED',   66, 58),
  ('Priya Sharma', 'priya@example.com',  '9000000007', 'Female', 'Hyderabad',  (SELECT id FROM courses WHERE name = 'Data Analytics'),         '2026-08-01', 'ENROLLED',    55, 48),
  ('Mohan Rao',    'mohan@example.com',  '9000000008', 'Male',   'Medak',      (SELECT id FROM courses WHERE name = 'Electrician'),            '2026-08-01', 'ENROLLED',    72, 62),
  ('Divya Nair',   'divya@example.com',  '9000000009', 'Female', 'Hyderabad',  (SELECT id FROM courses WHERE name = 'Full Stack Development'), '2026-08-01', 'DROPPED_OUT', 40, 45);

INSERT INTO outcomes (trainee_id, outcome_status, company_name, job_role, monthly_salary, joined_on, business_name, business_type, updated_by) VALUES
  ((SELECT id FROM trainees WHERE email = 'asha@example.com'),    'PLACED',        'Sample Tech Pvt Ltd', 'Junior Developer', 28000, '2026-08-15', NULL, NULL, 'ADMIN'),
  ((SELECT id FROM trainees WHERE email = 'ravi@example.com'),    'SEEKING_JOB',   NULL, NULL, NULL, NULL, NULL, NULL, 'TRAINEE'),
  ((SELECT id FROM trainees WHERE email = 'sneha@example.com'),   'PLACED',        'Sample Data Co',      'Data Analyst',     32000, '2026-09-01', NULL, NULL, 'TRAINEE'),
  ((SELECT id FROM trainees WHERE email = 'imran@example.com'),   'SELF_EMPLOYED', NULL, NULL, NULL, '2026-07-01', 'Khan Electricals', 'Home wiring', 'ADMIN'),
  ((SELECT id FROM trainees WHERE email = 'lakshmi@example.com'), 'SELF_EMPLOYED', NULL, NULL, NULL, '2026-06-15', 'Lakshmi Tailors',  'Stitching',   'ADMIN');
-- Suresh Babu has no outcome row on purpose: he shows as "Awaiting reply".
