// The form fields used by BOTH the admin "Update outcome" popup and the trainee's email page.
// The extra fields change depending on the status that is chosen.

export const emptyOutcome = {
  outcomeStatus: "",
  companyName: "",
  jobRole: "",
  monthlySalary: "",
  joinedOn: "",
  businessName: "",
  businessType: "",
};

// Turns the form values into what the backend expects (empty text becomes null)
export function toPayload(form) {
  return {
    outcomeStatus: form.outcomeStatus,
    companyName: form.companyName || null,
    jobRole: form.jobRole || null,
    monthlySalary: form.monthlySalary === "" ? null : Number(form.monthlySalary),
    joinedOn: form.joinedOn || null,
    businessName: form.businessName || null,
    businessType: form.businessType || null,
  };
}

export default function OutcomeFields({ form, setForm }) {
  const set = (key) => (e) => setForm({ ...form, [key]: e.target.value });

  return (
    <>
      <label>
        What are you doing now?
        <select value={form.outcomeStatus} onChange={set("outcomeStatus")} required>
          <option value="">Select...</option>
          <option value="PLACED">Placed in a job</option>
          <option value="SEEKING_JOB">Seeking a job</option>
          <option value="SELF_EMPLOYED">Self-employed</option>
        </select>
      </label>

      {form.outcomeStatus === "PLACED" && (
        <>
          <label>
            Company name
            <input value={form.companyName} onChange={set("companyName")} required />
          </label>
          <label>
            Job role
            <input value={form.jobRole} onChange={set("jobRole")} required />
          </label>
          <div className="row">
            <label>
              Monthly salary (Rs.)
              <input type="number" min="0" value={form.monthlySalary} onChange={set("monthlySalary")} />
            </label>
            <label>
              Joining date
              <input type="date" value={form.joinedOn} onChange={set("joinedOn")} />
            </label>
          </div>
        </>
      )}

      {form.outcomeStatus === "SELF_EMPLOYED" && (
        <>
          <label>
            Business name
            <input value={form.businessName} onChange={set("businessName")} required />
          </label>
          <div className="row">
            <label>
              Type of work
              <input value={form.businessType} onChange={set("businessType")} />
            </label>
            <label>
              Started on
              <input type="date" value={form.joinedOn} onChange={set("joinedOn")} />
            </label>
          </div>
        </>
      )}
    </>
  );
}
