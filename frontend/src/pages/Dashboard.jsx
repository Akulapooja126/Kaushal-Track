import { useEffect, useState } from "react";
import {
  Bar, BarChart, CartesianGrid, Cell, Legend, Line, LineChart, Pie, PieChart,
  ResponsiveContainer, Tooltip, XAxis, YAxis,
} from "recharts";
import api, { errMsg } from "../api.js";
import StatCard from "../components/StatCard.jsx";

const COLORS = ["#16a34a", "#f59e0b", "#1e3a8a", "#9ca3af"];

export default function Dashboard() {
  const [data, setData] = useState(null);
  const [trainees, setTrainees] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    Promise.all([api.get("/api/dashboard"), api.get("/api/trainees")])
      .then(([d, t]) => {
        setData(d.data);
        setTrainees(t.data);
      })
      .catch((e) => setError(errMsg(e)));
  }, []);

  if (error) return <p className="error">{error}</p>;
  if (!data) return <p className="muted">Loading...</p>;

  const alerts = trainees.filter((t) => t.riskLevel !== "LOW");
  const hasOutcomes = data.outcomeDistribution.some((o) => o.value > 0);

  return (
    <>
      <h2>Dashboard</h2>
      <p className="sub">Overview of trainees, attendance, assessment and outcomes</p>

      <div className="cards">
        <StatCard label="Total Trainees" value={data.totalTrainees} />
        <StatCard label="Completed" value={data.completed} />
        <StatCard label="Placed" value={data.placed} />
        <StatCard label="Seeking Job" value={data.seekingJob} />
        <StatCard label="Self Employed" value={data.selfEmployed} />
        <StatCard label="High Risk" value={data.highRisk} />
        <StatCard label="Medium Risk" value={data.mediumRisk} />
        <StatCard label="Low Risk" value={data.lowRisk} />
      </div>

      <div className="cards">
        <StatCard label="Employment / Self-employment" value={`${data.employmentRate}%`} />
        <StatCard label="Average Attendance" value={`${data.avgAttendance}%`} />
        <StatCard label="Average Assessment" value={`${data.avgAssessment}%`} />
        <StatCard label="Needs Intervention" value={`${data.needsIntervention}%`} />
      </div>

      <div className="grid2">
        <div className="card">
          <h3>Program Performance</h3>
          {data.programs.length === 0 ? (
            <p className="muted">No program data yet. Add trainees first.</p>
          ) : (
            <div className="chart">
              <ResponsiveContainer>
                <BarChart data={data.programs}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="course" tick={{ fontSize: 11 }} />
                  <YAxis domain={[0, 100]} />
                  <Tooltip />
                  <Legend />
                  <Bar dataKey="avgAttendance" name="Avg attendance %" fill="#1e3a8a" />
                  <Bar dataKey="avgAssessment" name="Avg assessment %" fill="#16a34a" />
                </BarChart>
              </ResponsiveContainer>
            </div>
          )}
        </div>

        <div className="card">
          <h3>Outcome Distribution</h3>
          {!hasOutcomes ? (
            <p className="muted">No outcomes yet. They appear once trainees complete a course.</p>
          ) : (
            <div className="chart">
              <ResponsiveContainer>
                <PieChart>
                  <Pie data={data.outcomeDistribution} dataKey="value" nameKey="name" outerRadius={90} label>
                    {data.outcomeDistribution.map((_, i) => (
                      <Cell key={i} fill={COLORS[i % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip />
                  <Legend />
                </PieChart>
              </ResponsiveContainer>
            </div>
          )}
        </div>
      </div>

      <div className="card">
        <h3>Employment Outcome Trend</h3>
        {data.trend.length === 0 ? (
          <p className="muted">Joining dates are needed to show the trend.</p>
        ) : (
          <div className="chart">
            <ResponsiveContainer>
              <LineChart data={data.trend}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="month" />
                <YAxis allowDecimals={false} />
                <Tooltip />
                <Line type="monotone" dataKey="count" name="Started job / business" stroke="#1e3a8a" strokeWidth={2} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        )}
      </div>

      <div className={alerts.length > 0 ? "card alert" : "card"}>
        <h3>Early Intervention Alerts</h3>
        {alerts.length === 0 ? (
          <p className="muted">No trainees currently need extra support.</p>
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr><th>Name</th><th>Course</th><th>Attendance</th><th>Assessment</th><th>Risk</th><th>Why</th></tr>
              </thead>
              <tbody>
                {alerts.map((t) => (
                  <tr key={t.id}>
                    <td>{t.fullName}</td>
                    <td>{t.courseName}</td>
                    <td>{t.attendancePct}%</td>
                    <td>{t.assessmentScore}%</td>
                    <td><span className={`badge ${t.riskLevel}`}>{t.riskLevel}</span></td>
                    <td>{t.riskReason}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </>
  );
}
