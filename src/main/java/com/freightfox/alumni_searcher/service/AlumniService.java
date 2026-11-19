
package com.freightfox.alumni_searcher.service;

import com.freightfox.alumni_searcher.dto.AlumniSearchRequest;
import com.freightfox.alumni_searcher.dto.AlumniResponse;

import java.util.List;

public interface AlumniService {

    List<AlumniResponse> searchAlumni(AlumniSearchRequest request);

    List<AlumniResponse> getAllAlumni();
}
