package com.kaushaltrack.backend.service;

import com.kaushaltrack.backend.model.OutcomeInvite;
import com.kaushaltrack.backend.model.Trainee;
import com.kaushaltrack.backend.repository.InviteRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/** Sends the "tell us your job status" email after a course is completed. */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final InviteRepository inviteRepo;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    public EmailService(JavaMailSender mailSender, InviteRepository inviteRepo) {
        this.mailSender = mailSender;
        this.inviteRepo = inviteRepo;
    }

    /** Saves a secret link in the database, emails it, and records whether the email worked. */
    public boolean sendOutcomeRequest(Trainee trainee) {
        OutcomeInvite invite = new OutcomeInvite();
        invite.setTraineeId(trainee.getId());
        invite.setToken(UUID.randomUUID().toString().replace("-", ""));
        inviteRepo.save(invite);

        String link = frontendUrl + "/outcome/" + invite.getToken();
        String text = "Dear " + trainee.getFullName() + ",\n\n"
                + "Congratulations on completing your course: " + trainee.getCourse().getName() + ".\n\n"
                + "Please tell us what you are doing now (placed in a job, seeking a job, or self-employed) "
                + "using this link:\n" + link + "\n\n"
                + "The link is valid for 30 days.\n\nThank you,\nKaushalTrack Team";

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            if (fromAddress != null && !fromAddress.isBlank()) message.setFrom(fromAddress);
            message.setTo(trainee.getEmail());
            message.setSubject("Please share your job status - KaushalTrack");
            message.setText(text);
            mailSender.send(message);
            invite.setEmailOk(true);
        } catch (Exception e) {
            // Never break saving a trainee just because email failed
            log.warn("Could not send email to {}: {}", trainee.getEmail(), e.getMessage());
            invite.setEmailOk(false);
        }
        inviteRepo.save(invite);
        return Boolean.TRUE.equals(invite.getEmailOk());
    }
}
