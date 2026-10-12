package com.kaushaltrack.backend.dto;

import java.time.LocalDate;

/** Outcome details. Used by the admin form and by the trainee's email link. */
public record OutcomeForm(
        String outcomeStatus,   // PLACED, SEEKING_JOB or SELF_EMPLOYED
        String companyName,
        String jobRole,
        Double monthlySalary,
        LocalDate joinedOn,
        String businessName,
        String businessType) {
}
