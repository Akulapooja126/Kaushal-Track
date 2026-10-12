package com.kaushaltrack.backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "outcomes")
public class Outcome {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long traineeId;

    private String outcomeStatus;

    private String companyName;

    private String jobRole;

    private Double monthlySalary;

    private LocalDate joinedOn;

    private String businessName;

    private String businessType;

    private String updatedBy;

    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTraineeId() { return traineeId; }
    public void setTraineeId(Long traineeId) { this.traineeId = traineeId; }
    public String getOutcomeStatus() { return outcomeStatus; }
    public void setOutcomeStatus(String outcomeStatus) { this.outcomeStatus = outcomeStatus; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getJobRole() { return jobRole; }
    public void setJobRole(String jobRole) { this.jobRole = jobRole; }
    public Double getMonthlySalary() { return monthlySalary; }
    public void setMonthlySalary(Double monthlySalary) { this.monthlySalary = monthlySalary; }
    public LocalDate getJoinedOn() { return joinedOn; }
    public void setJoinedOn(LocalDate joinedOn) { this.joinedOn = joinedOn; }
    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }
    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
