
package com.freightfox.alumni_searcher.integration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LinkedInSearchUrlBuilderTest {

    private final LinkedInSearchUrlBuilder builder =
            new LinkedInSearchUrlBuilder();

    @Test
    void shouldBuildSearchUrlForDifferentUniversities() {

        String url1 = builder.build(
                "CMR University",
                "Java Developer"
        );

        String url2 = builder.build(
                "Anna University",
                "Python Developer"
        );

        assertTrue(url1.contains("Java%20Developer%20CMR%20University"));
        assertTrue(url2.contains("Python%20Developer%20Anna%20University"));

        assertNotEquals(url1, url2);
    }

    @Test
    void shouldEncodeSpecialCharactersInSearchQuery() {

        String url = builder.build(
                "University of Technology",
                "Software Engineer"
        );

        assertTrue(url.contains("Software%20Engineer%20University%20of%20Technology"));
    }
}
