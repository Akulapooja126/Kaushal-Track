import { NavLink, Outlet, Route, Routes } from "react-router-dom";
import Dashboard from "./pages/Dashboard.jsx";
import Trainees from "./pages/Trainees.jsx";
import OutcomeTracker from "./pages/OutcomeTracker.jsx";
import SkillGaps from "./pages/SkillGaps.jsx";
import RiskEngine from "./pages/RiskEngine.jsx";
import PublicOutcome from "./pages/PublicOutcome.jsx";

// Header + menu shown on every admin page
function Layout() {
  const tabs = [
    { to: "/", label: "Dashboard", end: true },
    { to: "/trainees", label: "Trainees" },
    { to: "/outcomes", label: "Outcome Tracker" },
    { to: "/skill-gaps", label: "Skill Gap Analytics" },
    { to: "/risk", label: "Risk Engine" },
  ];
  return (
    <>
      <header className="header">
        <h1>KaushalTrack</h1>
        <p>Outcome &amp; Impact Tracking for Skilling Initiatives</p>
      </header>
      <nav className="nav">
        {tabs.map((t) => (
          <NavLink key={t.to} to={t.to} end={t.end} className={({ isActive }) => (isActive ? "tab active" : "tab")}>
            {t.label}
          </NavLink>
        ))}
      </nav>
      <main className="container">
        <Outlet />
      </main>
    </>
  );
}

export default function App() {
  return (
    <Routes>
      {/* The page trainees open from the email (no menu) */}
      <Route path="/outcome/:token" element={<PublicOutcome />} />

      <Route element={<Layout />}>
        <Route path="/" element={<Dashboard />} />
        <Route path="/trainees" element={<Trainees />} />
        <Route path="/outcomes" element={<OutcomeTracker />} />
        <Route path="/skill-gaps" element={<SkillGaps />} />
        <Route path="/risk" element={<RiskEngine />} />
      </Route>
    </Routes>
  );
}
