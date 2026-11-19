
package com.freightfox.alumni_searcher.controller;

import com.freightfox.alumni_searcher.service.AlumniService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AlumniController.class)
class AlumniControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AlumniService alumniService;

    @Test
    void shouldRejectRequestWhenUniversityIsMissing() throws Exception {

        String request = """
                {
                    "designation": "Java Developer"
                }
                """;

        mockMvc.perform(post("/api/alumni/search")
                        .contentType("application/json")
                        .content(request))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(alumniService);
    }

    @Test
    void shouldRejectRequestWhenDesignationIsMissing() throws Exception {

        String request = """
                {
                    "university": "Anna University"
                }
                """;

        mockMvc.perform(post("/api/alumni/search")
                        .contentType("application/json")
                        .content(request))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(alumniService);
    }

    @Test
    void shouldRejectInvalidPassoutYear() throws Exception {

        String request = """
                {
                    "university": "Anna University",
                    "designation": "Java Developer",
                    "passoutYear": -2023
                }
                """;

        mockMvc.perform(post("/api/alumni/search")
                        .contentType("application/json")
                        .content(request))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(alumniService);
    }
}
