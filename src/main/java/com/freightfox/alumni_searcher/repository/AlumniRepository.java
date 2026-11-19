
package com.freightfox.alumni_searcher.repository;

import com.freightfox.alumni_searcher.entity.Alumni;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlumniRepository extends JpaRepository<Alumni, Long> {

    Optional<Alumni> findByLinkedinProfileUrl(String linkedinProfileUrl);
}
