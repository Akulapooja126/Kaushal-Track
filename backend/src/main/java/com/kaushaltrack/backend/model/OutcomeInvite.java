package com.kaushaltrack.backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "outcome_requests")
public class OutcomeInvite {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long traineeId;

    private String token;

    private LocalDateTime sentAt;

    private LocalDateTime expiresAt;

    private Boolean emailOk;

    private LocalDateTime usedAt;

    @PrePersist
    void onCreate() {
        if (sentAt == null) sentAt = LocalDateTime.now();
        if (expiresAt == null) expiresAt = sentAt.plusDays(30);
        if (emailOk == null) emailOk = false;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTraineeId() { return traineeId; }
    public void setTraineeId(Long traineeId) { this.traineeId = traineeId; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public Boolean getEmailOk() { return emailOk; }
    public void setEmailOk(Boolean emailOk) { this.emailOk = emailOk; }
    public LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }
}
