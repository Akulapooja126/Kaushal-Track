package com.kaushaltrack.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/** Data sent by the Add / Edit trainee form. */
public record TraineeRequest(
        @NotBlank(message = "Full name is required") String fullName,
        @NotBlank(message = "Email is required") @Email(message = "Email is not valid") String email,
        String phone,
        String gender,
        String district,
        @NotNull(message = "Course is required") Long courseId,
        LocalDate enrolledOn,
        String status,
        @NotNull(message = "Attendance is required") Double attendancePct,
        @NotNull(message = "Assessment score is required") Double assessmentScore) {
}
