package com.kaushaltrack.backend.controller;

import com.kaushaltrack.backend.dto.OutcomeForm;
import com.kaushaltrack.backend.model.OutcomeInvite;
import com.kaushaltrack.backend.model.Trainee;
import com.kaushaltrack.backend.repository.InviteRepository;
import com.kaushaltrack.backend.repository.TraineeRepository;
import com.kaushaltrack.backend.service.OutcomeService;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** The page the trainee reaches from the email link. No login: the secret token is the key. */
@RestController
@RequestMapping("/api/public/outcome")
public class PublicOutcomeController {

    private final InviteRepository inviteRepo;
    private final TraineeRepository traineeRepo;
    private final OutcomeService outcomeService;

    public PublicOutcomeController(InviteRepository inviteRepo, TraineeRepository traineeRepo,
                                   OutcomeService outcomeService) {
        this.inviteRepo = inviteRepo;
        this.traineeRepo = traineeRepo;
        this.outcomeService = outcomeService;
    }

    @GetMapping("/{token}")
    public Map<String, Object> info(@PathVariable String token) {
        OutcomeInvite invite = findValid(token);
        Trainee trainee = traineeRepo.findById(invite.getTraineeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trainee not found"));
        return Map.of(
                "traineeName", trainee.getFullName(),
                "courseName", trainee.getCourse().getName(),
                "alreadySubmitted", invite.getUsedAt() != null);
    }

    @PostMapping("/{token}")
    public Map<String, String> submit(@PathVariable String token, @RequestBody OutcomeForm form) {
        OutcomeInvite invite = findValid(token);
        if (invite.getUsedAt() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "You have already submitted your details. Please contact the training centre to change them.");
        }
        outcomeService.save(invite.getTraineeId(), form, "TRAINEE");
        invite.setUsedAt(LocalDateTime.now());
        inviteRepo.save(invite);
        return Map.of("message", "Thank you! Your details have been saved.");
    }

    private OutcomeInvite findValid(String token) {
        OutcomeInvite invite = inviteRepo.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "This link is not valid"));
        if (invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.GONE, "This link has expired");
        }
        return invite;
    }
}
