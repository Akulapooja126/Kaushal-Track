package com.kaushaltrack.backend.service;

import com.kaushaltrack.backend.dto.OutcomeForm;
import com.kaushaltrack.backend.dto.OutcomeResponse;
import com.kaushaltrack.backend.model.Outcome;
import com.kaushaltrack.backend.model.OutcomeInvite;
import com.kaushaltrack.backend.model.Trainee;
import com.kaushaltrack.backend.repository.InviteRepository;
import com.kaushaltrack.backend.repository.OutcomeRepository;
import com.kaushaltrack.backend.repository.TraineeRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OutcomeService {

    private final OutcomeRepository outcomeRepo;
    private final TraineeRepository traineeRepo;
    private final InviteRepository inviteRepo;

    public OutcomeService(OutcomeRepository outcomeRepo, TraineeRepository traineeRepo,
                          InviteRepository inviteRepo) {
        this.outcomeRepo = outcomeRepo;
        this.traineeRepo = traineeRepo;
        this.inviteRepo = inviteRepo;
    }

    /** All COMPLETED trainees, with their outcome if they have given one. */
    @Transactional(readOnly = true)
    public List<OutcomeResponse> findAllCompleted() {
        Map<Long, Outcome> outcomes = new HashMap<>();
        for (Outcome o : outcomeRepo.findAll()) outcomes.put(o.getTraineeId(), o);

        return traineeRepo.findAll().stream()
                .filter(t -> "COMPLETED".equals(t.getStatus()))
                .map(t -> toResponse(t, outcomes.get(t.getId())))
                .toList();
    }

    /** Creates or updates the outcome of one trainee. updatedBy is ADMIN or TRAINEE. */
    @Transactional
    public OutcomeResponse save(Long traineeId, OutcomeForm f, String updatedBy) {
        Trainee trainee = traineeRepo.findById(traineeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trainee not found"));
        validate(f);

        Outcome o = outcomeRepo.findByTraineeId(traineeId).orElseGet(() -> {
            Outcome fresh = new Outcome();
            fresh.setTraineeId(traineeId);
            return fresh;
        });

        boolean placed = "PLACED".equals(f.outcomeStatus());
        boolean selfEmployed = "SELF_EMPLOYED".equals(f.outcomeStatus());

        o.setOutcomeStatus(f.outcomeStatus());
        o.setCompanyName(placed ? clean(f.companyName()) : null);
        o.setJobRole(placed ? clean(f.jobRole()) : null);
        o.setMonthlySalary(placed ? f.monthlySalary() : null);
        o.setJoinedOn((placed || selfEmployed) ? f.joinedOn() : null);
        o.setBusinessName(selfEmployed ? clean(f.businessName()) : null);
        o.setBusinessType(selfEmployed ? clean(f.businessType()) : null);
        o.setUpdatedBy(updatedBy);
        o.setUpdatedAt(LocalDateTime.now());
        outcomeRepo.save(o);

        return toResponse(trainee, o);
    }

    private void validate(OutcomeForm f) {
        String s = f.outcomeStatus();
        if (s == null || !List.of("PLACED", "SEEKING_JOB", "SELF_EMPLOYED").contains(s)) {
            throw bad("Please choose Placed, Seeking a job, or Self-employed");
        }
        if ("PLACED".equals(s)) {
            if (clean(f.companyName()) == null) throw bad("Company name is required");
            if (clean(f.jobRole()) == null) throw bad("Job role is required");
            if (f.monthlySalary() != null && f.monthlySalary() < 0) throw bad("Salary cannot be negative");
        }
        if ("SELF_EMPLOYED".equals(s) && clean(f.businessName()) == null) {
            throw bad("Business name is required");
        }
    }

    private static ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private static String clean(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private OutcomeResponse toResponse(Trainee t, Outcome o) {
        return new OutcomeResponse(
                t.getId(), t.getFullName(), t.getEmail(), t.getCourse().getName(),
                o == null ? null : o.getOutcomeStatus(),
                o == null ? null : o.getCompanyName(),
                o == null ? null : o.getJobRole(),
                o == null ? null : o.getMonthlySalary(),
                o == null ? null : o.getJoinedOn(),
                o == null ? null : o.getBusinessName(),
                o == null ? null : o.getBusinessType(),
                o == null ? null : o.getUpdatedBy(),
                o == null ? null : o.getUpdatedAt(),
                emailStatus(t.getId()));
    }

    private String emailStatus(Long traineeId) {
        Optional<OutcomeInvite> last = inviteRepo.findTopByTraineeIdOrderBySentAtDesc(traineeId);
        if (last.isEmpty()) return "Not sent";
        OutcomeInvite i = last.get();
        if (i.getUsedAt() != null) return "Replied";
        return Boolean.TRUE.equals(i.getEmailOk()) ? "Sent" : "Failed";
    }
}
