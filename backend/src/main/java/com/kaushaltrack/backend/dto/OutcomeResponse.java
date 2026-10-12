package com.kaushaltrack.backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** One row of the Outcome Tracker table (a completed trainee and their outcome). */
public record OutcomeResponse(
        Long traineeId,
        String traineeName,
        String email,
        String courseName,
        String outcomeStatus,   // null = still waiting for a reply
        String companyName,
        String jobRole,
        Double monthlySalary,
        LocalDate joinedOn,
        String businessName,
        String businessType,
        String updatedBy,
        LocalDateTime updatedAt,
        String emailStatus) {  // Not sent, Sent, Failed or Replied
}
