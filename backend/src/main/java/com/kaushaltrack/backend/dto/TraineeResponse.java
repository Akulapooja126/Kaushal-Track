package com.kaushaltrack.backend.dto;

import java.time.LocalDate;

/** One trainee row as shown on the website (includes course name and risk). */
public record TraineeResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        String gender,
        String district,
        Long courseId,
        String courseName,
        LocalDate enrolledOn,
        String status,
        Double attendancePct,
        Double assessmentScore,
        String riskLevel,
        String riskReason,
        String outcomeStatus) {
}
