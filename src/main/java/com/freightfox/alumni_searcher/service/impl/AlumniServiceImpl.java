
package com.freightfox.alumni_searcher.service.impl;

import com.freightfox.alumni_searcher.dto.AlumniSearchRequest;
import com.freightfox.alumni_searcher.dto.AlumniResponse;
import com.freightfox.alumni_searcher.entity.Alumni;
import com.freightfox.alumni_searcher.integration.PhantomBusterClient;
import com.freightfox.alumni_searcher.repository.AlumniRepository;
import com.freightfox.alumni_searcher.service.AlumniService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AlumniServiceImpl implements AlumniService {

    private final AlumniRepository alumniRepository;
    private final PhantomBusterClient phantomBusterClient;

    public AlumniServiceImpl(
            AlumniRepository alumniRepository,
            PhantomBusterClient phantomBusterClient) {

        this.alumniRepository = alumniRepository;
        this.phantomBusterClient = phantomBusterClient;
    }

    
@Override
@Transactional
public List<AlumniResponse> searchAlumni(AlumniSearchRequest request) {

    List<Alumni> profiles =
            phantomBusterClient.searchAlumni(request);

    List<Alumni> filteredProfiles = profiles.stream()
            .filter(alumni ->
                    request.getPassoutYear() == null ||
                    request.getPassoutYear().equals(alumni.getPassoutYear())
            )
            .toList();

    List<Alumni> savedProfiles = filteredProfiles.stream()
            .map(this::saveOrUpdate)
            .toList();

    return savedProfiles.stream()
            .map(this::mapToResponse)
            .toList();
}


    private Alumni saveOrUpdate(Alumni alumni) {

    if (alumni.getLinkedinProfileUrl() == null ||
            alumni.getLinkedinProfileUrl().isBlank()) {

        throw new IllegalArgumentException(
                "LinkedIn profile URL cannot be empty."
        );
    }

    return alumniRepository
            .findByLinkedinProfileUrl(alumni.getLinkedinProfileUrl())
            .map(existing -> {
                updateExisting(existing, alumni);
                return alumniRepository.save(existing);
            })
            .orElseGet(() -> alumniRepository.save(alumni));
}

    private void updateExisting(Alumni existing, Alumni updated) {
        existing.setName(updated.getName());
        existing.setCurrentRole(updated.getCurrentRole());
        existing.setUniversity(updated.getUniversity());
        existing.setLocation(updated.getLocation());
        existing.setLinkedinHeadline(updated.getLinkedinHeadline());
        existing.setPassoutYear(updated.getPassoutYear());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlumniResponse> getAllAlumni() {

        return alumniRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private AlumniResponse mapToResponse(Alumni alumni) {

        return new AlumniResponse(
                alumni.getId(),
                alumni.getName(),
                alumni.getCurrentRole(),
                alumni.getUniversity(),
                alumni.getLocation(),
                alumni.getLinkedinHeadline(),
                alumni.getPassoutYear(),
                alumni.getLinkedinProfileUrl()
        );
    }
}
