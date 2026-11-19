
package com.freightfox.alumni_searcher.integration;

import com.freightfox.alumni_searcher.entity.Alumni;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PhantomBusterCsvParserTest {

    private PhantomBusterCsvParser parser;

    @BeforeEach
    void setUp() {
        parser = new PhantomBusterCsvParser();
    }

    @Test
    void shouldParseValidJsonProfiles() {

        String json = """
                [
                    {
                        "fullName": "Rahul Kumar",
                        "jobTitle": "Java Developer",
                        "schoolName": "CMR University",
                        "location": "Bengaluru",
                        "headline": "Software Developer",
                        "linkedInUrl": "https://linkedin.com/in/rahul"
                    }
                ]
                """;

        List<Alumni> result = parser.parseJson(json);

        assertEquals(1, result.size());
        assertEquals("Rahul Kumar", result.get(0).getName());
        assertEquals("Java Developer", result.get(0).getCurrentRole());
        assertEquals("CMR University", result.get(0).getUniversity());
        assertEquals("Bengaluru", result.get(0).getLocation());
        assertEquals("Software Developer", result.get(0).getLinkedinHeadline());
        assertEquals("https://linkedin.com/in/rahul",
                result.get(0).getLinkedinProfileUrl());
        assertNull(result.get(0).getPassoutYear());
    }

    @Test
    void shouldUseProfileUrlWhenLinkedInUrlIsMissing() {

        String json = """
                [
                    {
                        "fullName": "Arun Kumar",
                        "schoolName": "CMR University",
                        "profileUrl": "https://linkedin.com/in/arun"
                    }
                ]
                """;

        List<Alumni> result = parser.parseJson(json);

        assertEquals(1, result.size());
        assertEquals("https://linkedin.com/in/arun",
                result.get(0).getLinkedinProfileUrl());
    }

    @Test
    void shouldSkipProfilesWithMissingRequiredFields() {

        String json = """
                [
                    {
                        "fullName": "Rahul Kumar",
                        "schoolName": "CMR University",
                        "profileUrl": "https://linkedin.com/in/rahul"
                    },
                    {
                        "fullName": "Arun Kumar",
                        "schoolName": "CMR University"
                    }
                ]
                """;

        List<Alumni> result = parser.parseJson(json);

        assertEquals(1, result.size());
        assertEquals("Rahul Kumar", result.get(0).getName());
    }

    @Test
    void shouldReturnEmptyListForEmptyJsonContent() {

        assertTrue(parser.parseJson("").isEmpty());
        assertTrue(parser.parseJson(null).isEmpty());
    }

    @Test
    void shouldRejectInvalidJson() {

        String invalidJson = "This is not valid JSON";

        assertThrows(
                IllegalStateException.class,
                () -> parser.parseJson(invalidJson)
        );
    }

    @Test
    void shouldRejectNonArrayJson() {

        String json = """
                {
                    "fullName": "Rahul Kumar"
                }
                """;

        assertThrows(
                IllegalStateException.class,
                () -> parser.parseJson(json)
        );
    }
    @Test
void shouldParseValidCsvProfiles() {

    String csv = """
            fullName,jobTitle,schoolName,location,headline,linkedInUrl
            Rahul Kumar,Java Developer,CMR University,Bengaluru,Software Developer,https://linkedin.com/in/rahul
            """;

    List<Alumni> result = parser.parse(csv);

    assertEquals(1, result.size());
    assertEquals("Rahul Kumar", result.get(0).getName());
    assertEquals("Java Developer", result.get(0).getCurrentRole());
    assertEquals("CMR University", result.get(0).getUniversity());
}

@Test
void shouldReturnEmptyListForEmptyCsvContent() {

    assertTrue(parser.parse("").isEmpty());
    assertTrue(parser.parse(null).isEmpty());
}
}
