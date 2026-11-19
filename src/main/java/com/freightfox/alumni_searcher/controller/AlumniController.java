
package com.freightfox.alumni_searcher.controller;

import com.freightfox.alumni_searcher.dto.AlumniSearchRequest;
import com.freightfox.alumni_searcher.dto.AlumniResponse;
import com.freightfox.alumni_searcher.service.AlumniService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/alumni")
@Tag(name = "Alumni API", description = "Search and retrieve alumni profiles")
public class AlumniController {

    private final AlumniService alumniService;

    public AlumniController(AlumniService alumniService) {
        this.alumniService = alumniService;
    }

    @Operation(
            summary = "Search alumni profiles",
            description = "Search LinkedIn alumni profiles using university and designation. Pass-out year is optional."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alumni profiles retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "502", description = "PhantomBuster service error"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/search")
    public ResponseEntity<com.freightfox.alumni_searcher.dto.ApiResponse<List<AlumniResponse>>> searchAlumni(
            @Valid @RequestBody AlumniSearchRequest request) {

        List<AlumniResponse> results =
                alumniService.searchAlumni(request);

        return ResponseEntity.ok(
                new com.freightfox.alumni_searcher.dto.ApiResponse<>("success", results)
        );
    }

    @Operation(
            summary = "Get all alumni",
            description = "Retrieve all alumni profiles stored in the database."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alumni profiles retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/all")
    public ResponseEntity<com.freightfox.alumni_searcher.dto.ApiResponse<List<AlumniResponse>>> getAllAlumni() {

        List<AlumniResponse> results = alumniService.getAllAlumni();

        return ResponseEntity.ok(
                new com.freightfox.alumni_searcher.dto.ApiResponse<>("success", results)
        );
    }
}
