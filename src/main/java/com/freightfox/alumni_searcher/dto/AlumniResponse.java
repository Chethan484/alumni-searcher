
package com.freightfox.alumni_searcher.dto;

public class AlumniResponse {

    private Long id;
    private String name;
    private String currentRole;
    private String university;
    private String location;
    private String linkedinHeadline;
    private Integer passoutYear;
    private String linkedinProfileUrl;

    public AlumniResponse() {
    }

    public AlumniResponse(
            Long id,
            String name,
            String currentRole,
            String university,
            String location,
            String linkedinHeadline,
            Integer passoutYear,
            String linkedinProfileUrl) {

        this.id = id;
        this.name = name;
        this.currentRole = currentRole;
        this.university = university;
        this.location = location;
        this.linkedinHeadline = linkedinHeadline;
        this.passoutYear = passoutYear;
        this.linkedinProfileUrl = linkedinProfileUrl;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCurrentRole() { return currentRole; }
    public String getUniversity() { return university; }
    public String getLocation() { return location; }
    public String getLinkedinHeadline() { return linkedinHeadline; }
    public Integer getPassoutYear() { return passoutYear; }
    public String getLinkedinProfileUrl() { return linkedinProfileUrl; }
}
