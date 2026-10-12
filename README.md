# KaushalTrack

Tracks trainees (attendance, assessment, course) and what they do after the course
(placed, seeking a job, self-employed). Built with Java (Spring Boot), React and Neon (PostgreSQL).

```
kaushaltrack/
  db/         schema_v2.sql (tables) and seed.sql (optional sample data)
  backend/    Java Spring Boot REST API
  frontend/   React website
  .vscode/    launch.json: your private settings for running the backend
```

## How it works (for explaining)

1. **Database (Neon)** stores courses, trainees, outcomes, and the email links.
2. **Backend** layers: `controller` (receives web requests) -> `service` (the logic) ->
   `repository` (talks to the database) -> `model` (one class per table).
3. **Frontend** pages call the backend with `axios` (see `src/api.js`) and draw tables and charts.
4. When a trainee is saved as **COMPLETED**, `TraineeService` calls `EmailService`. It stores a secret
   token, emails a link like `/outcome/<token>`, and the trainee fills the form on `PublicOutcome.jsx`.
   The admin can also enter the same details from the **Outcome Tracker** tab.
5. Risk is decided by simple rules in `RiskCalculator.java`.

## Step 1: Neon database

1. In Neon open **SQL Editor**, paste all of `db/schema_v2.sql`, click **Run**.
2. (Optional) paste `db/seed.sql` and run it to get sample data.
3. Click **Connect** on the project dashboard. Turn **Connection pooling OFF** and keep the details
   handy: host, database, user, password.

## Step 2: Run the backend

1. Open the `kaushaltrack` folder in VS Code (Java 21 and the Java extension packs installed).
2. Open `.vscode/launch.json` and fill in:
   - `DB_URL`: `jdbc:postgresql://<host>/<database>?sslmode=require`
   - `DB_USER`, `DB_PASSWORD`
   - `MAIL_USERNAME`, `MAIL_PASSWORD`: your Gmail and a Google **App password**
     (Google Account > Security > 2-Step Verification > App passwords).
     Email is optional: without it everything works, but emails show "Failed" in the Outcome Tracker.
3. Open `backend/src/main/java/com/kaushaltrack/backend/BackendApplication.java` and click **Run**
   above `main` (or press F5 and choose "Run KaushalTrack Backend").
4. Wait for `Started BackendApplication`, then open http://localhost:8080/api/courses.
   You should see a list (empty `[]` if you skipped the seed).

## Step 3: Run the frontend

```
cd frontend
npm install
npm run dev
```

Open http://localhost:5173.

## Step 4: Deploy

**Backend on Render** (Web Service)
- Root directory `backend`, Language **Docker**, region **Singapore**.
- Environment variables: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `MAIL_USERNAME`, `MAIL_PASSWORD`,
  `FRONTEND_URL` and `CORS_ORIGINS` (both = your Vercel address, no trailing slash).

**Frontend on Vercel**
- Root directory `frontend`, framework **Vite**.
- Environment variable `VITE_API_URL` = your Render address (no trailing slash).

Deploy the backend first, then the frontend, then put the Vercel address into Render's
`FRONTEND_URL` and `CORS_ORIGINS` and redeploy the backend.

Notes: Neon and Render free plans go to sleep, so the first page load after a break can take
30-60 seconds. As far as I know, Render's free plan blocks outgoing Gmail (SMTP), so emails may fail
there even though they work on your computer. If that happens, switch the mail sender to an
email API such as Resend or Brevo.

## Main API addresses

| Address | What it does |
|---|---|
| `GET/POST /api/courses` | list / add courses |
| `GET/POST/PUT/DELETE /api/trainees` | trainee add, edit, delete |
| `POST /api/trainees/{id}/send-outcome-email` | resend the email |
| `GET /api/outcomes`, `PUT /api/outcomes/{traineeId}` | outcome list / manual update |
| `GET /api/dashboard`, `GET /api/skill-gaps` | numbers for the charts |
| `GET/POST /api/public/outcome/{token}` | the trainee's email form |
