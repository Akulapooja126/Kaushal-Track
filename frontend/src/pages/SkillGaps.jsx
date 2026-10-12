import { useEffect, useState } from "react";
import { Bar, BarChart, CartesianGrid, Legend, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import api, { errMsg } from "../api.js";
import StatCard from "../components/StatCard.jsx";

export default function SkillGaps() {
  const [data, setData] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    api.get("/api/skill-gaps")
      .then((res) => setData(res.data))
      .catch((e) => setError(errMsg(e)));
  }, []);

  if (error) return <p className="error">{error}</p>;
  if (!data) return <p className="muted">Loading...</p>;

  const gaps = data.trainees.filter((t) => t.level !== "LOW");

  return (
    <>
      <h2>Skill Gap Analytics</h2>
      <p className="sub">
        Assessment scores compared with a sample industry benchmark of {data.benchmark}%.
        A gap of 25 points or more is High, 10 to 24 is Medium.
      </p>

      <div className="cards">
        <StatCard label="Average Assessment" value={`${data.avgAssessment}%`} />
        <StatCard label="High Skill Gap" value={data.highGap} />
        <StatCard label="Medium Skill Gap" value={data.mediumGap} />
        <StatCard label="Low Skill Gap" value={data.lowGap} />
      </div>

      <div className="card">
        <h3>Program-wise Assessment vs Benchmark</h3>
        {data.programs.length === 0 ? (
          <p className="muted">No data yet. Add trainees first.</p>
        ) : (
          <div className="chart">
            <ResponsiveContainer>
              <BarChart data={data.programs}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="course" tick={{ fontSize: 11 }} />
                <YAxis domain={[0, 100]} />
                <Tooltip />
                <Legend />
                <Bar dataKey="benchmark" name="Industry benchmark" fill="#9ca3af" />
                <Bar dataKey="avgAssessment" name="Trainee average" fill="#1e3a8a" />
              </BarChart>
            </ResponsiveContainer>
          </div>
        )}
      </div>

      <div className="card">
        <h3>Skill Gap Details</h3>
        {gaps.length === 0 ? (
          <p className="muted">No trainees have a skill gap right now.</p>
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr><th>Name</th><th>Course</th><th>Assessment</th><th>Gap (points)</th><th>Level</th></tr>
              </thead>
              <tbody>
                {gaps.map((t) => (
                  <tr key={t.id}>
                    <td>{t.fullName}</td>
                    <td>{t.course}</td>
                    <td>{t.assessmentScore}%</td>
                    <td>{t.gap}</td>
                    <td><span className={`badge ${t.level}`}>{t.level}</span></td>
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
