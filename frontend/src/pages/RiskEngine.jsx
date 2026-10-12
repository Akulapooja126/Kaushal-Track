import { useEffect, useState } from "react";
import api, { errMsg } from "../api.js";
import StatCard from "../components/StatCard.jsx";

export default function RiskEngine() {
  const [trainees, setTrainees] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    api.get("/api/trainees")
      .then((res) => setTrainees(res.data))
      .catch((e) => setError(errMsg(e)));
  }, []);

  const count = (level) => trainees.filter((t) => t.riskLevel === level).length;
  const atRisk = trainees
    .filter((t) => t.riskLevel !== "LOW")
    .sort((a, b) => (a.riskLevel === b.riskLevel ? 0 : a.riskLevel === "HIGH" ? -1 : 1));

  return (
    <>
      <h2>Risk Engine</h2>
      <p className="sub">Finds trainees who may need early support, using attendance and assessment score.</p>

      {error && <p className="error">{error}</p>}

      <div className="cards">
        <StatCard label="High Risk" value={count("HIGH")} />
        <StatCard label="Medium Risk" value={count("MEDIUM")} />
        <StatCard label="Low Risk" value={count("LOW")} />
      </div>

      <div className="card">
        <h3>How the risk is decided</h3>
        <p className="muted">
          <b>High:</b> attendance below 60% or assessment below 50%.<br />
          <b>Medium:</b> attendance below 75% or assessment below 65%.<br />
          <b>Low:</b> everything else.
        </p>
      </div>

      <div className={atRisk.length > 0 ? "card alert" : "card"}>
        <h3>Trainees who need support</h3>
        {atRisk.length === 0 ? (
          <p className="muted">No trainees are at risk right now.</p>
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr><th>Name</th><th>Course</th><th>Attendance</th><th>Assessment</th><th>Risk</th><th>Why</th></tr>
              </thead>
              <tbody>
                {atRisk.map((t) => (
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
