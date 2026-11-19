
package com.freightfox.alumni_searcher.integration;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import tools.jackson.databind.JsonNode;

import com.freightfox.alumni_searcher.dto.AlumniSearchRequest;
import com.freightfox.alumni_searcher.entity.Alumni;



@Component
public class PhantomBusterClientImpl implements PhantomBusterClient {

    private final RestClient apiClient;
    private final String agentId;
    private final PhantomBusterCsvParser parser;
    private final LinkedInSearchUrlBuilder urlBuilder;
    private final int maxAttempts;
    private final long pollIntervalMs;

    public PhantomBusterClientImpl(
            RestClient.Builder builder,
            PhantomBusterCsvParser parser,
            LinkedInSearchUrlBuilder urlBuilder,
            @Value("${phantombuster.api.base-url}") String baseUrl,
            @Value("${phantombuster.api.key}") String apiKey,
            @Value("${phantombuster.agent-id}") String agentId,
            @Value("${phantombuster.execution.max-attempts:90}") int maxAttempts,
            @Value("${phantombuster.execution.poll-interval-ms:10000}") long pollIntervalMs) {

        this.parser = parser;
        this.urlBuilder = urlBuilder;
        this.agentId = agentId;
        this.maxAttempts = maxAttempts;
        this.pollIntervalMs = pollIntervalMs;

        this.apiClient = builder
                .baseUrl(baseUrl)
                .defaultHeader("X-Phantombuster-Key", apiKey)
                .build();
    }

    @Override
    public List<Alumni> searchAlumni(AlumniSearchRequest request) {

        // 1. Build a LinkedIn search URL from the API request.
        String searchUrl = urlBuilder.build(
                request.getUniversity(),
                request.getDesignation()
        );

        // 2. Launch a fresh PhantomBuster execution.
        Map<String, Object> launchBody = Map.of(
                "id", agentId,
                "bonusArgument", Map.of("searchUrl", searchUrl),
                "maxInstanceCount", 1
        );

        JsonNode launchResponse = apiClient.post()
                .uri("/agents/launch")
                .body(launchBody)
                .retrieve()
                .body(JsonNode.class);

        if (launchResponse == null
                || !launchResponse.hasNonNull("containerId")) {
            throw new IllegalStateException(
                    "PhantomBuster did not return a container ID."
            );
        }

        String containerId = launchResponse.get("containerId").asText();

        // 3. Wait until the execution finishes.
        waitForCompletion(containerId);

        // 4. Retrieve the JSON result object.
       
// 4. Retrieve the accumulated CSV.
String csvContent = fetchAccumulatedCsv();


// 5. Parse only records belonging to this search query.
String expectedDesignation = request.getDesignation().trim();

List<Alumni> profiles = parser.parseBySearchQuery(
        csvContent,
        expectedDesignation
);

        // 5. Parse the returned profile data.
        

        // 6. Filter profiles using the requested university and designation.
        String university = request.getUniversity().trim()
                .toLowerCase();

        String designation = request.getDesignation().trim()
                .toLowerCase();

        return profiles.stream()
                .filter(profile -> profile.getUniversity() != null
                        && profile.getUniversity().toLowerCase()
                        .contains(university))
                .filter(profile -> profile.getCurrentRole() != null
                        && matchesDesignation(
                                profile.getCurrentRole(),
                                designation
        ))
                .toList();
    }
    private boolean matchesDesignation(
        String currentRole,
        String requestedDesignation) {

    String role = currentRole.toLowerCase().trim();
    String requested = requestedDesignation.toLowerCase().trim();

    if (role.contains(requested)) {
        return true;
    }

    String[] requestedWords = requested.split("\\s+");

    return java.util.Arrays.stream(requestedWords)
            .allMatch(role::contains);
}
    private String fetchAccumulatedCsv() {

    JsonNode agentInfo = apiClient.get()
            .uri(uriBuilder -> uriBuilder
                    .path("/agents/fetch")
                    .queryParam("id", agentId)
                    .build())
            .retrieve()
            .body(JsonNode.class);

    if (agentInfo == null
            || !agentInfo.hasNonNull("orgS3Folder")
            || !agentInfo.hasNonNull("s3Folder")) {

        throw new IllegalStateException(
                "Could not retrieve PhantomBuster result-file information."
        );
    }

    String orgFolder = agentInfo.get("orgS3Folder").asText();
    String agentFolder = agentInfo.get("s3Folder").asText();

    String csvUrl = "https://phantombuster.s3.amazonaws.com/"
            + orgFolder + "/" + agentFolder + "/result.csv";

    String csvContent = downloadCsv(csvUrl);

    if (csvContent == null || csvContent.isBlank()) {
        throw new IllegalStateException(
                "PhantomBuster result CSV is empty."
        );
    }

    return csvContent;
}
protected String downloadCsv(String csvUrl) {
    return RestClient.create()
            .get()
            .uri(csvUrl)
            .retrieve()
            .body(String.class);
}

    private void waitForCompletion(String containerId) {

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {

            JsonNode statusResponse = apiClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/containers/fetch")
                            .queryParam("id", containerId)
                            .build())
                    .retrieve()
                    .body(JsonNode.class);

            if (statusResponse == null
                    || !statusResponse.hasNonNull("status")) {
                throw new IllegalStateException(
                        "Could not retrieve PhantomBuster execution status."
                );
            }

            String status = statusResponse.get("status").asText();

            if ("finished".equalsIgnoreCase(status)) {

                if (statusResponse.hasNonNull("exitCode")
                        && statusResponse.get("exitCode").asInt() != 0) {
                    throw new IllegalStateException(
                            "PhantomBuster execution failed. Exit code: "
                                    + statusResponse.get("exitCode").asText()
                    );
                }

                return;
            }

            if ("failed".equalsIgnoreCase(status)
                    || "error".equalsIgnoreCase(status)
                    || "stopped".equalsIgnoreCase(status)
                    || "killed".equalsIgnoreCase(status)) {

                throw new IllegalStateException(
                        "PhantomBuster execution ended with status: " + status
                );
            }

            try {
                Thread.sleep(pollIntervalMs);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();

                throw new IllegalStateException(
                        "Interrupted while waiting for PhantomBuster.",
                        exception
                );
            }
        }

        throw new IllegalStateException(
        "PhantomBuster execution timed out after "
                + (maxAttempts * pollIntervalMs / 1000)
                + " seconds."
        );
    }
}
