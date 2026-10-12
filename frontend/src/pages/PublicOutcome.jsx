import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import api, { errMsg } from "../api.js";
import OutcomeFields, { emptyOutcome, toPayload } from "../components/OutcomeFields.jsx";

// The page a trainee sees after clicking the link in the email.
export default function PublicOutcome() {
  const { token } = useParams();
  const [info, setInfo] = useState(null);
  const [form, setForm] = useState(emptyOutcome);
  const [error, setError] = useState("");
  const [done, setDone] = useState("");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    api.get(`/api/public/outcome/${token}`)
      .then((res) => setInfo(res.data))
      .catch((e) => setError(errMsg(e)));
  }, [token]);

  async function submit(e) {
    e.preventDefault();
    setSaving(true);
    setError("");
    try {
      const res = await api.post(`/api/public/outcome/${token}`, toPayload(form));
      setDone(res.data.message);
    } catch (err) {
      setError(errMsg(err));
    } finally {
      setSaving(false);
    }
  }

  return (
    <>
      <header className="header">
        <h1>KaushalTrack</h1>
        <p>Outcome &amp; Impact Tracking for Skilling Initiatives</p>
      </header>
      <main className="container narrow">
        {!info && error && <p className="error">{error}</p>}
        {!info && !error && <p className="muted">Loading...</p>}

        {info && (
          <div className="card">
            <h2>Hello, {info.traineeName}</h2>
            <p className="sub">Course: {info.courseName}. Please tell us what you are doing now.</p>

            {done ? (
              <p className="success">{done}</p>
            ) : info.alreadySubmitted ? (
              <p className="success">You have already submitted your details. Thank you!</p>
            ) : (
              <form className="form" onSubmit={submit}>
                {error && <p className="error">{error}</p>}
                <OutcomeFields form={form} setForm={setForm} />
                <div className="modal-actions">
                  <button className="btn" disabled={saving}>{saving ? "Saving..." : "Submit"}</button>
                </div>
              </form>
            )}
          </div>
        )}
      </main>
    </>
  );
}
