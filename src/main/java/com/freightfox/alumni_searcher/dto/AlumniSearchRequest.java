
package com.freightfox.alumni_searcher.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Max;

import io.swagger.v3.oas.annotations.media.Schema;



public class AlumniSearchRequest {

   @Schema(
    name = "university",
    description = "Name of the university to search for",
    example = "CMR University"
)
@NotBlank(message = "University name is required")
private String university;

@Schema(
    name = "designation",
    description = "Current job designation to search for",
    example = "Java Developer"
    )
    @NotBlank(message = "Designation is required")
    private String designation;

    @Schema(
        name = "passoutYear",
        description = "Optional graduation or pass-out year",
        example = "2023",
        nullable = true
    )
    @Positive(message = "Passout year must be positive")
    @Max(value = 2100, message = "Invalid passout year")
    private Integer passoutYear;

    public AlumniSearchRequest() {
    }

    public String getUniversity() {
        return university;
    }

    public void setUniversity(String university) {
        this.university = university;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public Integer getPassoutYear() {
        return passoutYear;
    }

    public void setPassoutYear(Integer passoutYear) {
        this.passoutYear = passoutYear;
    }
}
