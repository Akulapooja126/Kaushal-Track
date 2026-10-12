import { useEffect, useState } from "react";
import api, { errMsg } from "../api.js";
import OutcomeFields, {
  emptyOutcome,
  toPayload,
} from "../components/OutcomeFields.jsx";
import StatCard from "../components/StatCard.jsx";

export default function OutcomeTracker() {
  const [rows, setRows] = useState([]);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");

  // Popup for entering an outcome manually
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(emptyOutcome);
  const [formError, setFormError] = useState("");
  const [saving, setSaving] = useState(false);

  // Email sending state
  const [sendingId, setSendingId] = useState(null);

  async function load() {
    try {
      const res = await api.get("/api/outcomes");
      setRows(res.data);
      setError("");
    } catch (e) {
      setError(errMsg(e));
    }
  }

  useEffect(() => {
    load();
  }, []);

  function openEdit(r) {
    setEditing(r);
    setForm({
      outcomeStatus: r.outcomeStatus || "",
      companyName: r.companyName || "",
      jobRole: r.jobRole || "",
      monthlySalary: r.monthlySalary ?? "",
      joinedOn: r.joinedOn || "",
      businessName: r.businessName || "",
      businessType: r.businessType || "",
    });
    setFormError("");
  }

  async function save(e) {
    e.preventDefault();
    setSaving(true);
    setFormError("");
    setNotice("");

    try {
      await api.put(
        `/api/outcomes/${editing.traineeId}`,
        toPayload(form)
      );

      setEditing(null);
      setNotice("Outcome saved successfully.");
      await load();
    } catch (err) {
      setFormError(errMsg(err));
    } finally {
      setSaving(false);
    }
  }

  // Resend employment outcome email
  async function resendEmail(r) {
    setSendingId(r.traineeId);
    setNotice("");
    setError("");

    try {
      const res = await api.post(
        `/api/trainees/${r.traineeId}/send-outcome-email`
      );

      setNotice(
        res.data.message ||
          (res.data.sent
            ? "Email sent successfully."
            : "Email could not be sent. Check the mail settings.")
      );

      await load();
    } catch (e) {
      setError(errMsg(e));
    } finally {
      setSendingId(null);
    }
  }

  const count = (s) =>
    rows.filter((r) => r.outcomeStatus === s).length;

  const placed = count("PLACED");
  const self = count("SELF_EMPLOYED");

  const rate =
    rows.length === 0
      ? 0
      : Math.round(((placed + self) * 1000) / rows.length) / 10;

  function details(r) {
    if (r.outcomeStatus === "PLACED") {
      return (
        `${r.jobRole || ""} at ${r.companyName || ""}` +
        (r.monthlySalary
          ? `, Rs. ${r.monthlySalary}/month`
          : "")
      );
    }

    if (r.outcomeStatus === "SELF_EMPLOYED") {
      return (
        (r.businessName || "") +
        (r.businessType ? ` (${r.businessType})` : "")
      );
    }

    return "-";
  }

  return (
    <>
      <h2>Outcome Tracker</h2>

      <p className="sub">
        Completed trainees and what they are doing now. Trainees can
        reply through the email link, or you can enter the details
        yourself with Update.
      </p>

      {error && <p className="error">{error}</p>}
      {notice && <p className="success">{notice}</p>}

      <div className="cards">
        <StatCard
          label="Completed Trainees"
          value={rows.length}
        />
        <StatCard label="Placed" value={placed} />
        <StatCard
          label="Seeking Job"
          value={count("SEEKING_JOB")}
        />
        <StatCard label="Self Employed" value={self} />
        <StatCard
          label="Awaiting Reply"
          value={rows.filter((r) => !r.outcomeStatus).length}
        />
        <StatCard
          label="Employment Rate"
          value={`${rate}%`}
        />
      </div>

      <div className="card">
        <h3>Employment Outcome Details</h3>

        {rows.length === 0 ? (
          <p className="muted">
            No completed trainees yet. Set a trainee's status to
            Completed on the Trainees page.
          </p>
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Trainee</th>
                  <th>Course</th>
                  <th>Outcome</th>
                  <th>Details</th>
                  <th>Joined / started</th>
                  <th>Email</th>
                  <th>Updated by</th>
                  <th>Actions</th>
                </tr>
              </thead>

              <tbody>
                {rows.map((r) => (
                  <tr key={r.traineeId}>
                    <td>{r.traineeName}</td>
                    <td>{r.courseName}</td>

                    <td>
                      {r.outcomeStatus ? (
                        <span
                          className={`badge ${r.outcomeStatus}`}
                        >
                          {r.outcomeStatus.replace("_", " ")}
                        </span>
                      ) : (
                        <span className="badge pending">
                          AWAITING
                        </span>
                      )}
                    </td>

                    <td>{details(r)}</td>
                    <td>{r.joinedOn || "-"}</td>

                    <td>
                      <span
                        className={`badge ${
                          (r.emailStatus || "Not sent").split(" ")[0]
                        }`}
                      >
                        {r.emailStatus || "Not sent"}
                      </span>

                      <div style={{ marginTop: "6px" }}>
                        <button
                          type="button"
                          className="btn small"
                          disabled={sendingId === r.traineeId}
                          onClick={() => resendEmail(r)}
                        >
                          {sendingId === r.traineeId
                            ? "Sending..."
                            : "Resend Email"}
                        </button>
                      </div>
                    </td>

                    <td>{r.updatedBy || "-"}</td>

                    <td>
                      <button
                        type="button"
                        className="btn small"
                        onClick={() => openEdit(r)}
                      >
                        Update
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {editing && (
        <div className="overlay">
          <form className="modal form" onSubmit={save}>
            <h3>
              Update outcome: {editing.traineeName}
            </h3>

            {formError && (
              <p className="error">{formError}</p>
            )}

            <OutcomeFields
              form={form}
              setForm={setForm}
            />

            <div className="modal-actions">
              <button
                type="button"
                className="btn light"
                onClick={() => setEditing(null)}
              >
                Cancel
              </button>

              <button className="btn" disabled={saving}>
                {saving ? "Saving..." : "Save"}
              </button>
            </div>
          </form>
        </div>
      )}
    </>
  );
}
