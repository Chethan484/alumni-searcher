
package com.freightfox.alumni_searcher.integration;

import com.freightfox.alumni_searcher.dto.AlumniSearchRequest;
import com.freightfox.alumni_searcher.entity.Alumni;

import java.util.List;

public interface PhantomBusterClient {

    List<Alumni> searchAlumni(AlumniSearchRequest request);
}
