
package com.freightfox.alumni_searcher.integration;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import com.freightfox.alumni_searcher.entity.Alumni;

@Component
public class PhantomBusterCsvParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Alumni> parse(String csvContent) {
        return parseCsv(csvContent, null);
    }

    public List<Alumni> parseBySearchQuery(
            String csvContent,
            String expectedDesignation) {

        return parseCsv(csvContent, normalize(expectedDesignation));
    }

    private List<Alumni> parseCsv(
            String csvContent,
            String expectedDesignation) {

        List<Alumni> alumniList = new ArrayList<>();

        if (csvContent == null || csvContent.isBlank()) {
            return alumniList;
        }

        try (Reader reader = new StringReader(csvContent);
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreEmptyLines(true)
                     .setTrim(true)
                     .get()
                     .parse(reader)) {

            for (CSVRecord record : parser) {

                if (expectedDesignation != null) {
                    String query = getValue(record, "query");

                    if (query == null ||
                            !queryMatches(query, expectedDesignation)) {
                        continue;
                    }
                }

                Alumni alumni = createAlumni(
                        getValue(record, "fullName"),
                        getValue(record, "jobTitle"),
                        getValue(record, "schoolName"),
                        getValue(record, "location"),
                        getValue(record, "headline"),
                        getLinkedInUrl(record),
                        getPassoutYear(record)
                );

                if (alumni != null) {
                    alumniList.add(alumni);
                }
            }

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Failed to parse PhantomBuster CSV.",
                    exception
            );
        }

        return alumniList;
    }

    public List<Alumni> parseJson(String jsonContent) {

        List<Alumni> alumniList = new ArrayList<>();

        if (jsonContent == null || jsonContent.isBlank()) {
            return alumniList;
        }

        try {
            JsonNode profiles = objectMapper.readTree(jsonContent);

            if (!profiles.isArray()) {
                throw new IllegalStateException(
                        "PhantomBuster result is not a JSON array."
                );
            }

            for (JsonNode profile : profiles) {

                String linkedinUrl = getText(profile, "linkedInUrl");

                if (linkedinUrl == null) {
                    linkedinUrl = getText(profile, "profileUrl");
                }

                Alumni alumni = createAlumni(
                        getText(profile, "fullName"),
                        getText(profile, "jobTitle"),
                        getText(profile, "schoolName"),
                        getText(profile, "location"),
                        getText(profile, "headline"),
                        linkedinUrl,
                        getPassoutYear(profile)
                );

                if (alumni != null) {
                    alumniList.add(alumni);
                }
            }

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Failed to parse PhantomBuster JSON results.",
                    exception
            );
        }

        return alumniList;
    }

    private boolean queryMatches(
            String query,
            String expectedDesignation) {

        try {
            int queryStart = query.indexOf('?');

            String queryString = queryStart >= 0
                    ? query.substring(queryStart + 1)
                    : query;

            String keywords = null;

            for (String parameter : queryString.split("&")) {

                String[] parts = parameter.split("=", 2);

                if (parts.length == 2
                        && "keywords".equalsIgnoreCase(parts[0])) {

                    keywords = URLDecoder.decode(
                            parts[1],
                            StandardCharsets.UTF_8
                    );

                    break;
                }
            }

            if (keywords == null) {
                return false;
            }

            String normalizedKeywords = normalize(keywords);

            return normalizedKeywords.equals(expectedDesignation)
                    || normalizedKeywords.startsWith(
                            expectedDesignation + " "
                    );

        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private String normalize(String value) {
        return value == null
                ? ""
                : value.trim()
                        .replaceAll("\\s+", " ")
                        .toLowerCase();
    }

    private Alumni createAlumni(
            String name,
            String jobTitle,
            String university,
            String location,
            String headline,
            String linkedinUrl,
            Integer passoutYear) {

        if (name == null || university == null || linkedinUrl == null) {
            return null;
        }

        Alumni alumni = new Alumni();

        alumni.setName(name);
        alumni.setCurrentRole(jobTitle);
        alumni.setUniversity(university);
        alumni.setLocation(location);
        alumni.setLinkedinHeadline(headline);
        alumni.setLinkedinProfileUrl(linkedinUrl);
        alumni.setPassoutYear(passoutYear);

        return alumni;
    }

    private Integer getPassoutYear(CSVRecord record) {

        String[] possibleColumns = {
                "passoutYear",
                "graduationYear",
                "yearOfGraduation"
        };

        for (String column : possibleColumns) {
            Integer year = parseYear(getValue(record, column));

            if (year != null) {
                return year;
            }
        }

        return null;
    }

    private Integer getPassoutYear(JsonNode profile) {

        String[] possibleFields = {
                "passoutYear",
                "graduationYear",
                "yearOfGraduation"
        };

        for (String field : possibleFields) {
            Integer year = parseYear(getText(profile, field));

            if (year != null) {
                return year;
            }
        }

        return null;
    }

    private Integer parseYear(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            int year = Integer.parseInt(value.trim());

            if (year >= 1900 && year <= 2100) {
                return year;
            }

        } catch (NumberFormatException ignored) {
            // Invalid or unavailable graduation year.
        }

        return null;
    }

    private String getValue(CSVRecord record, String column) {

        if (!record.isMapped(column) || !record.isSet(column)) {
            return null;
        }

        String value = record.get(column);

        return value == null || value.isBlank()
                ? null
                : value.trim();
    }

    private String getLinkedInUrl(CSVRecord record) {

        String url = getValue(record, "linkedInUrl");

        return url != null ? url : getValue(record, "profileUrl");
    }

    private String getText(JsonNode node, String fieldName) {

        JsonNode value = node.get(fieldName);

        if (value == null || value.isNull() || value.asText().isBlank()) {
            return null;
        }

        return value.asText().trim();
    }
}
