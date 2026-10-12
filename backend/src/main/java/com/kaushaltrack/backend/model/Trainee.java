package com.kaushaltrack.backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "trainees")
public class Trainee {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    private String email;

    private String phone;

    private String gender;

    private String district;

    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "course_id")
    private Course course;

    private LocalDate enrolledOn;

    private String status;

    private Double attendancePct;

    private Double assessmentScore;

    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (enrolledOn == null) enrolledOn = LocalDate.now();
        if (status == null) status = "ENROLLED";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
    public LocalDate getEnrolledOn() { return enrolledOn; }
    public void setEnrolledOn(LocalDate enrolledOn) { this.enrolledOn = enrolledOn; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Double getAttendancePct() { return attendancePct; }
    public void setAttendancePct(Double attendancePct) { this.attendancePct = attendancePct; }
    public Double getAssessmentScore() { return assessmentScore; }
    public void setAssessmentScore(Double assessmentScore) { this.assessmentScore = assessmentScore; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
