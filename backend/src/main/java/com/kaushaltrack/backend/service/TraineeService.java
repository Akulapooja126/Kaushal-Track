package com.kaushaltrack.backend.service;

import com.kaushaltrack.backend.dto.TraineeRequest;
import com.kaushaltrack.backend.dto.TraineeResponse;
import com.kaushaltrack.backend.model.Course;
import com.kaushaltrack.backend.model.Outcome;
import com.kaushaltrack.backend.model.Trainee;
import com.kaushaltrack.backend.repository.CourseRepository;
import com.kaushaltrack.backend.repository.OutcomeRepository;
import com.kaushaltrack.backend.repository.TraineeRepository;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TraineeService {

    private static final List<String> STATUSES = List.of("ENROLLED", "COMPLETED", "DROPPED_OUT");

    private final TraineeRepository traineeRepo;
    private final CourseRepository courseRepo;
    private final OutcomeRepository outcomeRepo;
    private final EmailService emailService;

    public TraineeService(TraineeRepository traineeRepo, CourseRepository courseRepo,
                          OutcomeRepository outcomeRepo, EmailService emailService) {
        this.traineeRepo = traineeRepo;
        this.courseRepo = courseRepo;
        this.outcomeRepo = outcomeRepo;
        this.emailService = emailService;
    }

    @Transactional(readOnly = true)
    public List<TraineeResponse> findAll() {
        Map<Long, String> outcomes = outcomeStatusByTrainee();
        return traineeRepo.findAll().stream()
                .map(t -> toResponse(t, outcomes.get(t.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public TraineeResponse findOne(Long id) {
        Trainee t = getTrainee(id);
        String outcome = outcomeRepo.findByTraineeId(id).map(Outcome::getOutcomeStatus).orElse(null);
        return toResponse(t, outcome);
    }

    @Transactional
    public TraineeResponse create(TraineeRequest r) {
        Trainee t = new Trainee();
        apply(t, r);
        Trainee saved = traineeRepo.save(t);
        // A trainee added directly as COMPLETED also gets the email
        if ("COMPLETED".equals(saved.getStatus())) emailService.sendOutcomeRequest(saved);
        return toResponse(saved, null);
    }

    @Transactional
    public TraineeResponse update(Long id, TraineeRequest r) {
        Trainee t = getTrainee(id);
        boolean wasCompleted = "COMPLETED".equals(t.getStatus());
        apply(t, r);
        Trainee saved = traineeRepo.save(t);
        // Email goes out only at the moment the status changes to COMPLETED
        if (!wasCompleted && "COMPLETED".equals(saved.getStatus())) emailService.sendOutcomeRequest(saved);
        String outcome = outcomeRepo.findByTraineeId(id).map(Outcome::getOutcomeStatus).orElse(null);
        return toResponse(saved, outcome);
    }

    @Transactional
    public void delete(Long id) {
        getTrainee(id);
        traineeRepo.deleteById(id);   // outcomes and email links are removed by the database (ON DELETE CASCADE)
    }

    /** Used by the "Resend email" button. */
    @Transactional
    public boolean resendEmail(Long id) {
        Trainee t = getTrainee(id);
        if (!"COMPLETED".equals(t.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mark the trainee as COMPLETED first");
        }
        return emailService.sendOutcomeRequest(t);
    }

    // ---------- helpers ----------

    private Trainee getTrainee(Long id) {
        return traineeRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trainee not found"));
    }

    private void apply(Trainee t, TraineeRequest r) {
        Course course = courseRepo.findById(r.courseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Course not found"));
        String status = (r.status() == null || r.status().isBlank()) ? "ENROLLED" : r.status();
        if (!STATUSES.contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status must be ENROLLED, COMPLETED or DROPPED_OUT");
        }
        if (r.attendancePct() < 0 || r.attendancePct() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Attendance must be between 0 and 100");
        }
        if (r.assessmentScore() < 0 || r.assessmentScore() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assessment score must be between 0 and 100");
        }
        t.setFullName(r.fullName().trim());
        t.setEmail(r.email().trim());
        t.setPhone(blankToNull(r.phone()));
        t.setGender(blankToNull(r.gender()));
        t.setDistrict(blankToNull(r.district()));
        t.setCourse(course);
        if (r.enrolledOn() != null) t.setEnrolledOn(r.enrolledOn());
        else if (t.getEnrolledOn() == null) t.setEnrolledOn(LocalDate.now());
        t.setStatus(status);
        t.setAttendancePct(r.attendancePct());
        t.setAssessmentScore(r.assessmentScore());
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private Map<Long, String> outcomeStatusByTrainee() {
        Map<Long, String> map = new HashMap<>();
        for (Outcome o : outcomeRepo.findAll()) map.put(o.getTraineeId(), o.getOutcomeStatus());
        return map;
    }

    public static TraineeResponse toResponse(Trainee t, String outcomeStatus) {
        double attendance = t.getAttendancePct() == null ? 0 : t.getAttendancePct();
        double score = t.getAssessmentScore() == null ? 0 : t.getAssessmentScore();
        return new TraineeResponse(
                t.getId(), t.getFullName(), t.getEmail(), t.getPhone(), t.getGender(), t.getDistrict(),
                t.getCourse().getId(), t.getCourse().getName(),
                t.getEnrolledOn(), t.getStatus(), t.getAttendancePct(), t.getAssessmentScore(),
                RiskCalculator.level(attendance, score), RiskCalculator.reason(attendance, score),
                outcomeStatus);
    }
}
