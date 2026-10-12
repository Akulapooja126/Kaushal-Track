package com.kaushaltrack.backend.service;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple risk rules (easy to explain):
 *   HIGH   : attendance below 60  OR assessment below 50
 *   MEDIUM : attendance below 75  OR assessment below 65
 *   LOW    : everything else
 */
public final class RiskCalculator {

    private RiskCalculator() {
    }

    public static String level(double attendance, double score) {
        if (attendance < 60 || score < 50) return "HIGH";
        if (attendance < 75 || score < 65) return "MEDIUM";
        return "LOW";
    }

    public static String reason(double attendance, double score) {
        List<String> reasons = new ArrayList<>();
        if (attendance < 60) reasons.add("Attendance below 60%");
        else if (attendance < 75) reasons.add("Attendance below 75%");
        if (score < 50) reasons.add("Assessment below 50%");
        else if (score < 65) reasons.add("Assessment below 65%");
        return reasons.isEmpty() ? "On track" : String.join(", ", reasons);
    }
}
