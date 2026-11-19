
package com.freightfox.alumni_searcher.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import jakarta.persistence.Column;

@Entity
@Table(
    name = "alumni",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_alumni_linkedin_url",
            columnNames = "linkedin_profile_url"
        )
    }
)
public class Alumni {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "current_designation")
    private String currentRole;

    private String university;

    private String location;

    @Column(length = 1000)
    private String linkedinHeadline;

    private Integer passoutYear;

    @Column(name = "linkedin_profile_url", length = 500)
    private String linkedinProfileUrl;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Alumni() {
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCurrentRole() {
        return currentRole;
    }

    public void setCurrentRole(String currentRole) {
        this.currentRole = currentRole;
    }

    public String getUniversity() {
        return university;
    }

    public void setUniversity(String university) {
        this.university = university;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLinkedinHeadline() {
        return linkedinHeadline;
    }

    public void setLinkedinHeadline(String linkedinHeadline) {
        this.linkedinHeadline = linkedinHeadline;
    }

    public Integer getPassoutYear() {
        return passoutYear;
    }

    public void setPassoutYear(Integer passoutYear) {
        this.passoutYear = passoutYear;
    }

    public String getLinkedinProfileUrl() {
        return linkedinProfileUrl;
    }

    public void setLinkedinProfileUrl(String linkedinProfileUrl) {
        this.linkedinProfileUrl = linkedinProfileUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
