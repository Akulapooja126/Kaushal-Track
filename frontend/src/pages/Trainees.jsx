import { useEffect, useState } from "react";
import api, { errMsg } from "../api.js";

const emptyForm = {
  fullName: "", email: "", phone: "", gender: "", district: "",
  courseId: "", enrolledOn: "", status: "ENROLLED", attendancePct: "", assessmentScore: "",
};

const RISK_ORDER = { HIGH: 0, MEDIUM: 1, LOW: 2 };

export default function Trainees() {
  const [trainees, setTrainees] = useState([]);
  const [courses, setCourses] = useState([]);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");

  // search and filters
  const [search, setSearch] = useState("");
  const [courseFilter, setCourseFilter] = useState("");
  const [riskFilter, setRiskFilter] = useState("");
  const [statusFilter, setStatusFilter] = useState("");
  const [sortBy, setSortBy] = useState("");

  // popup form (editingId = null means "adding a new trainee")
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState("");

  async function load() {
    try {
      const [t, c] = await Promise.all([api.get("/api/trainees"), api.get("/api/courses")]);
      setTrainees(t.data);
      setCourses(c.data);
      setError("");
    } catch (e) {
      setError(errMsg(e));
    }
  }

  useEffect(() => {
    load();
  }, []);

  function openAdd() {
    setEditingId(null);
    setForm(emptyForm);
    setFormError("");
    setShowForm(true);
  }

  function openEdit(t) {
    setEditingId(t.id);
    setForm({
      fullName: t.fullName, email: t.email, phone: t.phone || "", gender: t.gender || "",
      district: t.district || "", courseId: t.courseId, enrolledOn: t.enrolledOn || "",
      status: t.status, attendancePct: t.attendancePct, assessmentScore: t.assessmentScore,
    });
    setFormError("");
    setShowForm(true);
  }

  async function save(e) {
    e.preventDefault();
    setSaving(true);
    setFormError("");
    const payload = {
      ...form,
      courseId: Number(form.courseId),
      attendancePct: form.attendancePct === "" ? null : Number(form.attendancePct),
      assessmentScore: form.assessmentScore === "" ? null : Number(form.assessmentScore),
      enrolledOn: form.enrolledOn || null,
    };
    try {
      const wasCompleted = editingId && trainees.find((t) => t.id === editingId)?.status === "COMPLETED";
      if (editingId) await api.put(`/api/trainees/${editingId}`, payload);
      else await api.post("/api/trainees", payload);
      setShowForm(false);
      setNotice(
        form.status === "COMPLETED" && !wasCompleted
          ? "Saved. An email asking for the job status was sent to the trainee (see Outcome Tracker for its status)."
          : "Saved."
      );
      load();
    } catch (err) {
      setFormError(errMsg(err));
    } finally {
      setSaving(false);
    }
  }

  async function remove(t) {
    if (!window.confirm(`Delete ${t.fullName}? This also removes their outcome details.`)) return;
    try {
      await api.delete(`/api/trainees/${t.id}`);
      setNotice("Trainee deleted.");
      load();
    } catch (e) {
      setError(errMsg(e));
    }
  }

  async function resend(t) {
    try {
      const res = await api.post(`/api/trainees/${t.id}/send-outcome-email`);
      setNotice(res.data.message);
    } catch (e) {
      setError(errMsg(e));
    }
  }

  async function addCourse() {
    const name = window.prompt("New course name:");
    if (!name) return;
    try {
      await api.post("/api/courses", { name });
      load();
    } catch (e) {
      setError(errMsg(e));
    }
  }

  // apply search, filters and sorting
  let rows = trainees.filter((t) => {
    const text = `${t.fullName} ${t.courseName}`.toLowerCase();
    return (
      text.includes(search.toLowerCase()) &&
      (!courseFilter || String(t.courseId) === courseFilter) &&
      (!riskFilter || t.riskLevel === riskFilter) &&
      (!statusFilter || t.status === statusFilter)
    );
  });
  if (sortBy === "name") rows = [...rows].sort((a, b) => a.fullName.localeCompare(b.fullName));
  if (sortBy === "attendance") rows = [...rows].sort((a, b) => b.attendancePct - a.attendancePct);
  if (sortBy === "assessment") rows = [...rows].sort((a, b) => b.assessmentScore - a.assessmentScore);
  if (sortBy === "risk") rows = [...rows].sort((a, b) => RISK_ORDER[a.riskLevel] - RISK_ORDER[b.riskLevel]);

  const setField = (key) => (e) => setForm({ ...form, [key]: e.target.value });

  return (
    <>
      <h2>Trainees</h2>
      <p className="sub">Add, edit and monitor training participants</p>

      {error && <p className="error">{error}</p>}
      {notice && <p className="success">{notice}</p>}

      <div className="toolbar">
        <button className="btn" onClick={openAdd}>+ Add Trainee</button>
        <button className="btn light" onClick={addCourse}>+ Add Course</button>
        <input placeholder="Search name or course..." value={search} onChange={(e) => setSearch(e.target.value)} />
        <select value={courseFilter} onChange={(e) => setCourseFilter(e.target.value)}>
          <option value="">All courses</option>
          {courses.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
        <select value={riskFilter} onChange={(e) => setRiskFilter(e.target.value)}>
          <option value="">All risk levels</option>
          <option value="HIGH">High</option>
          <option value="MEDIUM">Medium</option>
          <option value="LOW">Low</option>
        </select>
        <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
          <option value="">All statuses</option>
          <option value="ENROLLED">Enrolled</option>
          <option value="COMPLETED">Completed</option>
          <option value="DROPPED_OUT">Dropped out</option>
        </select>
        <select value={sortBy} onChange={(e) => setSortBy(e.target.value)}>
          <option value="">Default order</option>
          <option value="name">Name (A-Z)</option>
          <option value="attendance">Attendance (high to low)</option>
          <option value="assessment">Assessment (high to low)</option>
          <option value="risk">Risk (high first)</option>
        </select>
      </div>

      <div className="card">
        <p className="muted">Showing {rows.length} of {trainees.length} trainees</p>
        {rows.length === 0 ? (
          <p className="muted">No trainees found. Click "Add Trainee" to create one.</p>
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Name</th><th>Course</th><th>Attendance</th><th>Assessment</th>
                  <th>Status</th><th>Risk</th><th>Outcome</th><th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {rows.map((t) => (
                  <tr key={t.id}>
                    <td>{t.fullName}<br /><span className="muted">{t.email}</span></td>
                    <td>{t.courseName}</td>
                    <td>{t.attendancePct}%</td>
                    <td>{t.assessmentScore}%</td>
                    <td><span className={`badge ${t.status}`}>{t.status.replace("_", " ")}</span></td>
                    <td><span className={`badge ${t.riskLevel}`}>{t.riskLevel}</span></td>
                    <td>
                      {t.outcomeStatus
                        ? <span className={`badge ${t.outcomeStatus}`}>{t.outcomeStatus.replace("_", " ")}</span>
                        : <span className="muted">-</span>}
                    </td>
                    <td className="actions">
                      <button className="btn small light" onClick={() => openEdit(t)}>Edit</button>
                      {t.status === "COMPLETED" && (
                        <button className="btn small light" onClick={() => resend(t)}>Email</button>
                      )}
                      <button className="btn small danger" onClick={() => remove(t)}>Delete</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {showForm && (
        <div className="overlay">
          <form className="modal form" onSubmit={save}>
            <h3>{editingId ? "Edit Trainee" : "Add Trainee"}</h3>
            {formError && <p className="error">{formError}</p>}

            <label>Full name
              <input value={form.fullName} onChange={setField("fullName")} required />
            </label>
            <div className="row">
              <label>Email
                <input type="email" value={form.email} onChange={setField("email")} required />
              </label>
              <label>Phone
                <input value={form.phone} onChange={setField("phone")} />
              </label>
            </div>
            <div className="row">
              <label>Gender
                <select value={form.gender} onChange={setField("gender")}>
                  <option value="">Select...</option>
                  <option>Male</option>
                  <option>Female</option>
                  <option>Other</option>
                </select>
              </label>
              <label>District
                <input value={form.district} onChange={setField("district")} />
              </label>
            </div>
            <div className="row">
              <label>Course
                <select value={form.courseId} onChange={setField("courseId")} required>
                  <option value="">Select...</option>
                  {courses.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
                </select>
              </label>
              <label>Enrolled on
                <input type="date" value={form.enrolledOn} onChange={setField("enrolledOn")} />
              </label>
            </div>
            <div className="row">
              <label>Attendance (%)
                <input type="number" min="0" max="100" step="0.1" value={form.attendancePct}
                       onChange={setField("attendancePct")} required />
              </label>
              <label>Assessment score (%)
                <input type="number" min="0" max="100" step="0.1" value={form.assessmentScore}
                       onChange={setField("assessmentScore")} required />
              </label>
            </div>
            <label>Status
              <select value={form.status} onChange={setField("status")}>
                <option value="ENROLLED">Enrolled</option>
                <option value="COMPLETED">Completed (sends the job status email)</option>
                <option value="DROPPED_OUT">Dropped out</option>
              </select>
            </label>

            <div className="modal-actions">
              <button type="button" className="btn light" onClick={() => setShowForm(false)}>Cancel</button>
              <button className="btn" disabled={saving}>{saving ? "Saving..." : "Save"}</button>
            </div>
          </form>
        </div>
      )}
    </>
  );
}
