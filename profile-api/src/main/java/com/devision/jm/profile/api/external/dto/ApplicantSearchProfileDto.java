package com.devision.jm.profile.api.external.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Applicant Search Profile DTO
 *
 * DTO for applicant search criteria (Premium feature).
 *
 * Implements requirements:
 * - 6.2.1: Desired Technical Skills (tags)
 * - 6.2.2: Desired Employment Status (multiple selection)
 * - 6.2.3: Desired Country & Salary Range
 * - 6.2.4: Desired Education Degree
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicantSearchProfileDto {

    /**
     * 6.2.1: Desired Technical Skills (tags)
     * e.g., ["Java", "Spring Boot", "React", "AWS"]
     */
    @Size(max = 50, message = "Maximum 50 skills allowed")
    private List<String> desiredTechnicalSkills;

    /**
     * 6.2.2: Desired Employment Status (multiple selection)
     * e.g., ["FULL_TIME", "PART_TIME", "REMOTE"]
     */
    @Size(max = 10, message = "Maximum 10 employment status options allowed")
    private List<String> desiredEmploymentStatus;

    /**
     * 6.2.3: Desired Country
     */
    @Size(max = 100, message = "Country must be less than 100 characters")
    private String desiredCountry;

    /**
     * 6.2.3: Desired Salary Range - Minimum
     */
    @DecimalMin(value = "0", message = "Minimum salary must be non-negative")
    private BigDecimal desiredSalaryMin;

    /**
     * 6.2.3: Desired Salary Range - Maximum
     */
    @DecimalMin(value = "0", message = "Maximum salary must be non-negative")
    private BigDecimal desiredSalaryMax;

    /**
     * Salary Currency (e.g., USD, VND, EUR)
     */
    @Size(max = 10, message = "Currency must be less than 10 characters")
    private String salaryCurrency;

    /**
     * 6.2.4: Desired Education Degree
     * e.g., "BACHELOR", "MASTER", "NO_REQUIREMENT"
     */
    private String desiredEducationDegree;
}
