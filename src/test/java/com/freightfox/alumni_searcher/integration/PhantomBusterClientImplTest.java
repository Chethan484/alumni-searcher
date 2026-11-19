
package com.freightfox.alumni_searcher.integration;

import com.freightfox.alumni_searcher.dto.AlumniSearchRequest;
import com.freightfox.alumni_searcher.entity.Alumni;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class PhantomBusterClientImplTest {

    private TestablePhantomBusterClient client;
    private MockRestServiceServer mockServer;

    private static final String BASE_URL =
            "https://api.phantombuster.test/api/v2";

    private AlumniSearchRequest createRequest() {
        AlumniSearchRequest request = new AlumniSearchRequest();
        request.setUniversity("CMR University");
        request.setDesignation("Java Developer");
        return request;
    }

    @BeforeEach
    void setUp() {

        RestClient.Builder builder = RestClient.builder();

        mockServer = MockRestServiceServer
                .bindTo(builder)
                .build();

        client = new TestablePhantomBusterClient(
                builder,
                new PhantomBusterCsvParser(),
                new LinkedInSearchUrlBuilder()
        );
    }

    private void mockAgentLaunch(String containerId) {
        mockServer.expect(requestTo(BASE_URL + "/agents/launch"))
                .andRespond(withSuccess(
                        """
                        {"containerId":"%s"}
                        """.formatted(containerId),
                        MediaType.APPLICATION_JSON
                ));
    }

    private void mockExecutionFinished(String containerId) {
        mockServer.expect(requestTo(
                BASE_URL + "/containers/fetch?id=" + containerId))
                .andRespond(withSuccess(
                        """
                        {"status":"finished","exitCode":0}
                        """,
                        MediaType.APPLICATION_JSON
                ));
    }

    private void mockAgentFetch() {
        mockServer.expect(requestTo(
                BASE_URL + "/agents/fetch?id=test-agent-id"))
                .andRespond(withSuccess(
                        """
                        {
                            "orgS3Folder":"test-org",
                            "s3Folder":"test-folder"
                        }
                        """,
                        MediaType.APPLICATION_JSON
                ));
    }

    @Test
    void shouldLaunchAgentAndReturnMatchingProfiles() {

        String csvContent = """
                query,fullName,jobTitle,schoolName,location,headline,profileUrl
                "https://www.linkedin.com/search/results/people/?keywords=Java%20Developer%20CMR%20University","Rahul Kumar","Java Developer","CMR University","Bengaluru","Developer","https://linkedin.com/in/rahul"
                """;

        client.setCsvContent(csvContent);

        mockAgentLaunch("test-container-123");
        mockExecutionFinished("test-container-123");
        mockAgentFetch();

        List<Alumni> result = client.searchAlumni(createRequest());

        assertEquals(1, result.size());
        assertEquals("Rahul Kumar", result.get(0).getName());
        assertEquals("Java Developer", result.get(0).getCurrentRole());

        mockServer.verify();
    }

    @Test
    void shouldThrowExceptionWhenPhantomBusterExecutionFails() {

        mockAgentLaunch("failed-container");

        mockServer.expect(requestTo(
                BASE_URL + "/containers/fetch?id=failed-container"))
                .andRespond(withSuccess(
                        """
                        {"status":"failed"}
                        """,
                        MediaType.APPLICATION_JSON
                ));

        assertThrows(
                IllegalStateException.class,
                () -> client.searchAlumni(createRequest())
        );

        mockServer.verify();
    }

    @Test
    void shouldThrowExceptionWhenContainerIdIsMissing() {

        mockServer.expect(requestTo(BASE_URL + "/agents/launch"))
                .andRespond(withSuccess(
                        """
                        {"message":"Agent launch failed"}
                        """,
                        MediaType.APPLICATION_JSON
                ));

        assertThrows(
                IllegalStateException.class,
                () -> client.searchAlumni(createRequest())
        );

        mockServer.verify();
    }

    @Test
    void shouldFilterProfilesByUniversityAndDesignation() {

        String csvContent = """
                query,fullName,jobTitle,schoolName,location,headline,profileUrl
                "https://www.linkedin.com/search/results/people/?keywords=Java%20Developer%20CMR%20University","Rahul Kumar","Java Developer","CMR University","Bengaluru","Developer","https://linkedin.com/in/rahul"
                "https://www.linkedin.com/search/results/people/?keywords=Java%20Developer%20CMR%20University","Arun Kumar","Python Developer","CMR University","Bengaluru","Developer","https://linkedin.com/in/arun"
                "https://www.linkedin.com/search/results/people/?keywords=Java%20Developer%20CMR%20University","Vijay Kumar","Java Developer","Other University","Bengaluru","Developer","https://linkedin.com/in/vijay"
                """;

        client.setCsvContent(csvContent);

        mockAgentLaunch("filter-container");
        mockExecutionFinished("filter-container");
        mockAgentFetch();

        List<Alumni> result = client.searchAlumni(createRequest());

        assertEquals(1, result.size());
        assertEquals("Rahul Kumar", result.get(0).getName());

        mockServer.verify();
    }

    @Test
    void shouldMatchProfilesWhenUniversityIsInSchoolFilter() {

        String csvContent = """
                query,fullName,jobTitle,schoolName,location,headline,profileUrl
                "https://www.linkedin.com/search/results/people/?keywords=Java%20Developer&origin=FACETED_SEARCH&schoolFilter=%5B%229266819%22%5D","Rahul Kumar","Java Developer","CMR University","Bengaluru","Developer","https://linkedin.com/in/rahul"
                "https://www.linkedin.com/search/results/people/?keywords=Java%20Developer&origin=FACETED_SEARCH&schoolFilter=%5B%229266819%22%5D","Arun Kumar","Python Developer","CMR University","Bengaluru","Developer","https://linkedin.com/in/arun"
                "https://www.linkedin.com/search/results/people/?keywords=Java%20Developer&origin=FACETED_SEARCH&schoolFilter=%5B%229266819%22%5D","Vijay Kumar","Java Developer","Other University","Bengaluru","Developer","https://linkedin.com/in/vijay"
                """;

        client.setCsvContent(csvContent);

        mockAgentLaunch("school-filter-container");
        mockExecutionFinished("school-filter-container");
        mockAgentFetch();

        List<Alumni> result = client.searchAlumni(createRequest());

        assertEquals(1, result.size());
        assertEquals("Rahul Kumar", result.get(0).getName());
        assertEquals("CMR University", result.get(0).getUniversity());

        mockServer.verify();
    }

    @Test
    void shouldThrowExceptionWhenExitCodeIsNonZero() {

        mockAgentLaunch("error-container");

        mockServer.expect(requestTo(
                BASE_URL + "/containers/fetch?id=error-container"))
                .andRespond(withSuccess(
                        """
                        {"status":"finished","exitCode":1}
                        """,
                        MediaType.APPLICATION_JSON
                ));

        assertThrows(
                IllegalStateException.class,
                () -> client.searchAlumni(createRequest())
        );

        mockServer.verify();
    }

    @Test
    void shouldThrowExceptionWhenCsvIsEmpty() {

        client.setCsvContent("");

        mockAgentLaunch("empty-container");
        mockExecutionFinished("empty-container");
        mockAgentFetch();

        assertThrows(
                IllegalStateException.class,
                () -> client.searchAlumni(createRequest())
        );

        mockServer.verify();
    }

    private static class TestablePhantomBusterClient
            extends PhantomBusterClientImpl {

        private String csvContent = "";

        TestablePhantomBusterClient(
                RestClient.Builder builder,
                PhantomBusterCsvParser parser,
                LinkedInSearchUrlBuilder urlBuilder) {

            super(
                    builder,
                    parser,
                    urlBuilder,
                    BASE_URL,
                    "test-api-key",
                    "test-agent-id",
                    3,
                    1L
            );
        }

        void setCsvContent(String csvContent) {
            this.csvContent = csvContent;
        }

        @Override
        protected String downloadCsv(String csvUrl) {
            return csvContent;
        }
    }
}
