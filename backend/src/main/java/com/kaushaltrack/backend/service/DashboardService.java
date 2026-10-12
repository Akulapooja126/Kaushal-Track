package com.kaushaltrack.backend.service;

import com.kaushaltrack.backend.model.Outcome;
import com.kaushaltrack.backend.model.Trainee;
import com.kaushaltrack.backend.repository.OutcomeRepository;
import com.kaushaltrack.backend.repository.TraineeRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Calculates the numbers for the Dashboard and Skill Gap pages. */
@Service
public class DashboardService {

    private final TraineeRepository traineeRepo;
    private final OutcomeRepository outcomeRepo;

    @Value("${app.benchmark:75}")
    private double benchmark;

    public DashboardService(TraineeRepository traineeRepo, OutcomeRepository outcomeRepo) {
        this.traineeRepo = traineeRepo;
        this.outcomeRepo = outcomeRepo;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> dashboard() {
        List<Trainee> trainees = traineeRepo.findAll();
        Map<Long, Outcome> outcomes = outcomesByTrainee();

        int placed = 0, seeking = 0, selfEmployed = 0, awaiting = 0, completed = 0;
        int high = 0, medium = 0, low = 0;
        double attendanceSum = 0, scoreSum = 0;

        for (Trainee t : trainees) {
            attendanceSum += value(t.getAttendancePct());
            scoreSum += value(t.getAssessmentScore());

            String risk = RiskCalculator.level(value(t.getAttendancePct()), value(t.getAssessmentScore()));
            if (risk.equals("HIGH")) high++;
            else if (risk.equals("MEDIUM")) medium++;
            else low++;

            if ("COMPLETED".equals(t.getStatus())) {
                completed++;
                Outcome o = outcomes.get(t.getId());
                if (o == null) awaiting++;
                else if ("PLACED".equals(o.getOutcomeStatus())) placed++;
                else if ("SEEKING_JOB".equals(o.getOutcomeStatus())) seeking++;
                else if ("SELF_EMPLOYED".equals(o.getOutcomeStatus())) selfEmployed++;
            }
        }

        int total = trainees.size();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalTrainees", total);
        result.put("completed", completed);
        result.put("placed", placed);
        result.put("seekingJob", seeking);
        result.put("selfEmployed", selfEmployed);
        result.put("awaiting", awaiting);
        result.put("highRisk", high);
        result.put("mediumRisk", medium);
        result.put("lowRisk", low);
        result.put("avgAttendance", total == 0 ? 0 : round(attendanceSum / total));
        result.put("avgAssessment", total == 0 ? 0 : round(scoreSum / total));
        result.put("employmentRate", completed == 0 ? 0 : round((placed + selfEmployed) * 100.0 / completed));
        result.put("needsIntervention", total == 0 ? 0 : round((high + medium) * 100.0 / total));
        result.put("programs", programStats(trainees, outcomes));
        result.put("outcomeDistribution", List.of(
                named("Placed", placed),
                named("Seeking job", seeking),
                named("Self-employed", selfEmployed),
                named("Awaiting reply", awaiting)));
        result.put("trend", trend(outcomes));
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> skillGaps() {
        List<Trainee> trainees = traineeRepo.findAll();
        int high = 0, medium = 0, low = 0;
        double scoreSum = 0;
        List<Map<String, Object>> rows = new ArrayList<>();

        for (Trainee t : trainees) {
            double score = value(t.getAssessmentScore());
            double gap = Math.max(0, benchmark - score);
            String level = gap >= 25 ? "HIGH" : gap >= 10 ? "MEDIUM" : "LOW";
            if (level.equals("HIGH")) high++;
            else if (level.equals("MEDIUM")) medium++;
            else low++;
            scoreSum += score;

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", t.getId());
            row.put("fullName", t.getFullName());
            row.put("course", t.getCourse().getName());
            row.put("assessmentScore", score);
            row.put("gap", round(gap));
            row.put("level", level);
            rows.add(row);
        }

        Map<String, List<Trainee>> byCourse = groupByCourse(trainees);
        List<Map<String, Object>> programs = new ArrayList<>();
        for (Map.Entry<String, List<Trainee>> e : byCourse.entrySet()) {
            double sum = 0;
            for (Trainee t : e.getValue()) sum += value(t.getAssessmentScore());
            Map<String, Object> p = new LinkedHashMap<>();
            p.put("course", e.getKey());
            p.put("avgAssessment", round(sum / e.getValue().size()));
            p.put("benchmark", benchmark);
            programs.add(p);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("benchmark", benchmark);
        result.put("avgAssessment", trainees.isEmpty() ? 0 : round(scoreSum / trainees.size()));
        result.put("highGap", high);
        result.put("mediumGap", medium);
        result.put("lowGap", low);
        result.put("programs", programs);
        result.put("trainees", rows);
        return result;
    }

    // ---------- helpers ----------

    private List<Map<String, Object>> programStats(List<Trainee> trainees, Map<Long, Outcome> outcomes) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map.Entry<String, List<Trainee>> e : groupByCourse(trainees).entrySet()) {
            double attendance = 0, score = 0;
            int highRisk = 0, positive = 0;
            for (Trainee t : e.getValue()) {
                attendance += value(t.getAttendancePct());
                score += value(t.getAssessmentScore());
                if (RiskCalculator.level(value(t.getAttendancePct()), value(t.getAssessmentScore())).equals("HIGH")) highRisk++;
                Outcome o = outcomes.get(t.getId());
                if (o != null && ("PLACED".equals(o.getOutcomeStatus()) || "SELF_EMPLOYED".equals(o.getOutcomeStatus()))) positive++;
            }
            int n = e.getValue().size();
            Map<String, Object> p = new LinkedHashMap<>();
            p.put("course", e.getKey());
            p.put("trainees", n);
            p.put("avgAttendance", round(attendance / n));
            p.put("avgAssessment", round(score / n));
            p.put("highRisk", highRisk);
            p.put("positiveOutcomes", positive);
            list.add(p);
        }
        return list;
    }

    /** Number of people who started a job or business in each month. */
    private List<Map<String, Object>> trend(Map<Long, Outcome> outcomes) {
        TreeMap<String, Integer> months = new TreeMap<>();
        for (Outcome o : outcomes.values()) {
            boolean positive = "PLACED".equals(o.getOutcomeStatus()) || "SELF_EMPLOYED".equals(o.getOutcomeStatus());
            if (positive && o.getJoinedOn() != null) {
                months.merge(o.getJoinedOn().toString().substring(0, 7), 1, Integer::sum);
            }
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map.Entry<String, Integer> e : months.entrySet()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("month", e.getKey());
            m.put("count", e.getValue());
            list.add(m);
        }
        return list;
    }

    private Map<String, List<Trainee>> groupByCourse(List<Trainee> trainees) {
        Map<String, List<Trainee>> map = new LinkedHashMap<>();
        for (Trainee t : trainees) {
            map.computeIfAbsent(t.getCourse().getName(), k -> new ArrayList<>()).add(t);
        }
        return map;
    }

    private Map<Long, Outcome> outcomesByTrainee() {
        Map<Long, Outcome> map = new HashMap<>();
        for (Outcome o : outcomeRepo.findAll()) map.put(o.getTraineeId(), o);
        return map;
    }

    private static Map<String, Object> named(String name, int value) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", name);
        m.put("value", value);
        return m;
    }

    private static double value(Double d) {
        return d == null ? 0 : d;
    }

    private static double round(double d) {
        return Math.round(d * 10) / 10.0;
    }
}
