
package com.freightfox.alumni_searcher.service;

import com.freightfox.alumni_searcher.dto.AlumniSearchRequest;
import com.freightfox.alumni_searcher.dto.AlumniResponse;
import com.freightfox.alumni_searcher.entity.Alumni;
import com.freightfox.alumni_searcher.integration.PhantomBusterClient;
import com.freightfox.alumni_searcher.repository.AlumniRepository;
import com.freightfox.alumni_searcher.service.impl.AlumniServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlumniServiceImplTest {

    @Mock
    private AlumniRepository alumniRepository;

    @Mock
    private PhantomBusterClient phantomBusterClient;

    @InjectMocks
    private AlumniServiceImpl alumniService;

    private Alumni alumni;

    @BeforeEach
    void setUp() {

        alumni = new Alumni();

        alumni.setName("Kalaivanan K");
        alumni.setCurrentRole("Java Specialist");
        alumni.setUniversity("CMR University");
        alumni.setLocation("Bengaluru, Karnataka, India");
        alumni.setLinkedinHeadline("Student at CMR University");
        alumni.setLinkedinProfileUrl(
                "https://www.linkedin.com/in/kalaivanan-k-b6587125b/"
        );
    }

    @Test
    void shouldSaveNewAlumni() {

        AlumniSearchRequest request = new AlumniSearchRequest();

        request.setUniversity("CMR University");
        request.setDesignation("Java");

        when(phantomBusterClient.searchAlumni(request))
                .thenReturn(List.of(alumni));

        when(alumniRepository.findByLinkedinProfileUrl(
                alumni.getLinkedinProfileUrl()))
                .thenReturn(Optional.empty());

        when(alumniRepository.save(any(Alumni.class)))
                .thenAnswer(invocation -> {
                    Alumni saved = invocation.getArgument(0);
                    saved.setId(1L);
                    return saved;
                });

        List<AlumniResponse> result =
                alumniService.searchAlumni(request);

        assertEquals(1, result.size());
        assertEquals("Kalaivanan K", result.get(0).getName());

        verify(alumniRepository, times(1))
                .save(any(Alumni.class));

        verify(phantomBusterClient, times(1))
                .searchAlumni(request);
    }

    @Test
    void shouldUpdateExistingAlumniInsteadOfCreatingDuplicate() {

        AlumniSearchRequest request = new AlumniSearchRequest();

        request.setUniversity("CMR University");
        request.setDesignation("Java");

        Alumni existing = new Alumni();

        existing.setId(20L);
        existing.setName("Old Name");
        existing.setCurrentRole("Java Developer");
        existing.setUniversity("CMR University");
        existing.setLinkedinProfileUrl(
                alumni.getLinkedinProfileUrl()
        );

        when(phantomBusterClient.searchAlumni(request))
                .thenReturn(List.of(alumni));

        when(alumniRepository.findByLinkedinProfileUrl(
                alumni.getLinkedinProfileUrl()))
                .thenReturn(Optional.of(existing));

        when(alumniRepository.save(any(Alumni.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<AlumniResponse> result =
                alumniService.searchAlumni(request);

        assertEquals(1, result.size());
        assertEquals(20L, result.get(0).getId());
        assertEquals("Kalaivanan K", result.get(0).getName());

        verify(alumniRepository, times(1))
                .save(existing);
    }

    @Test
    void shouldRejectAlumniWithoutLinkedInUrl() {

        AlumniSearchRequest request = new AlumniSearchRequest();

        request.setUniversity("CMR University");
        request.setDesignation("Java");

        alumni.setLinkedinProfileUrl(null);

        when(phantomBusterClient.searchAlumni(request))
                .thenReturn(List.of(alumni));

        assertThrows(
                IllegalArgumentException.class,
                () -> alumniService.searchAlumni(request)
        );

        verify(alumniRepository, never())
                .save(any(Alumni.class));
    }

    @Test
    void shouldReturnAllAlumni() {

        when(alumniRepository.findAll())
                .thenReturn(List.of(alumni));

        List<AlumniResponse> result =
                alumniService.getAllAlumni();

        assertEquals(1, result.size());
        assertEquals("Kalaivanan K", result.get(0).getName());

        verify(alumniRepository, times(1)).findAll();
    }
}
