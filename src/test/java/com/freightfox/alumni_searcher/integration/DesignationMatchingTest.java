
package com.freightfox.alumni_searcher.integration;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class DesignationMatchingTest {

    private final PhantomBusterClientImpl client =
            new PhantomBusterClientImpl(
                    org.springframework.web.client.RestClient.builder(),
                    new PhantomBusterCsvParser(),
                    new LinkedInSearchUrlBuilder(),
                    "https://api.phantombuster.com/api/v2",
                    "test-key",
                    "test-agent",
                    1,
                    0
            );

    private boolean matches(String role, String requested) throws Exception {
        Method method = PhantomBusterClientImpl.class.getDeclaredMethod(
                "matchesDesignation",
                String.class,
                String.class
        );

        method.setAccessible(true);

        return (boolean) method.invoke(client, role, requested);
    }

    @Test
    void shouldMatchExactDesignation() throws Exception {
        assertTrue(matches("Java Developer", "Java Developer"));
    }

    @Test
    void shouldMatchSeniorDesignation() throws Exception {
        assertTrue(matches("Senior Java Developer", "Java Developer"));
    }

    @Test
    void shouldMatchFullStackDesignation() throws Exception {
        assertTrue(matches("Java Full Stack Developer", "Java Developer"));
    }

    @Test
    void shouldMatchInternDesignation() throws Exception {
        assertTrue(matches("Java Developer Intern", "Java Developer"));
    }

    @Test
    void shouldRejectUnrelatedDesignation() throws Exception {
        assertFalse(matches("Android Developer", "Java Developer"));
    }
}
