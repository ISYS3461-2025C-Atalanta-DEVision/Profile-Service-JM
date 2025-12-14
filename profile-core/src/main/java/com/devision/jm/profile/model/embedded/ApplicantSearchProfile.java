package com.devision.jm.profile.model.embedded;

import com.devision.jm.profile.model.enums.EducationDegree;
import com.devision.jm.profile.model.enums.EmploymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.util.List;

/**
 * Applicant Search Profile Embedded Document
 *
 * Contains the criteria for searching applicants (Premium feature).
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
public class ApplicantSearchProfile {

    /**
     * 6.2.1: Desired Technical Skills (tags)
     * List of skill tags the company is looking for
     */
    @Field("desired_technical_skills")
    private List<String> desiredTechnicalSkills;

    /**
     * 6.2.2: Desired Employment Status (multiple selection)
     * e.g., FULL_TIME, PART_TIME, CONTRACT, REMOTE
     */
    @Field("desired_employment_status")
    private List<EmploymentStatus> desiredEmploymentStatus;

    /**
     * 6.2.3: Desired Country
     * The country where the company is looking for applicants
     */
    @Field("desired_country")
    private String desiredCountry;

    /**
     * 6.2.3: Desired Salary Range - Minimum
     */
    @Field("desired_salary_min")
    private BigDecimal desiredSalaryMin;

    /**
     * 6.2.3: Desired Salary Range - Maximum
     */
    @Field("desired_salary_max")
    private BigDecimal desiredSalaryMax;

    /**
     * 6.2.3: Salary Currency (e.g., USD, VND, EUR)
     */
    @Field("salary_currency")
    @Builder.Default
    private String salaryCurrency = "USD";

    /**
     * 6.2.4: Desired Education Degree
     * Minimum education level required
     */
    @Field("desired_education_degree")
    private EducationDegree desiredEducationDegree;
}
