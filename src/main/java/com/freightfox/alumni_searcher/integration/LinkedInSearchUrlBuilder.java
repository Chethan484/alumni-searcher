
package com.freightfox.alumni_searcher.integration;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;

@Component
public class LinkedInSearchUrlBuilder {

    public String build(String university, String designation) {

        String searchQuery = designation + " " + university;

        String encodedQuery = URLEncoder.encode(
            searchQuery,
            StandardCharsets.UTF_8
        ).replace("+", "%20");

        return "https://www.linkedin.com/search/results/people/"
                + "?keywords=" + encodedQuery
                + "&origin=GLOBAL_SEARCH_HEADER";
    }
}
