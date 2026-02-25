package com.itmentorcommunityplatform.profileservice.controller;

import com.itmentorcommunityplatform.profileservice.docs.GetAllProfilesDocs;
import com.itmentorcommunityplatform.profileservice.dto.response.AllProfilesPaginatedResponseDto;
import com.itmentorcommunityplatform.profileservice.exception.ForbiddenException;
import com.itmentorcommunityplatform.profileservice.service.AdminProfileService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/profile/admin")
@RequiredArgsConstructor
public class ProfileAdminController {

    private final AdminProfileService  adminProfileService;

    @GetMapping("/profiles")
    @GetAllProfilesDocs
    public ResponseEntity<AllProfilesPaginatedResponseDto> getAllProfiles(@RequestParam("page_size") @Min(1) int pageSize,
                                                                          @RequestParam("page_number") @Min(1) int pageNumber,
                                                                          @RequestParam(required = false) Map<String, String> detailFilters,
                                                                          @RequestHeader(value = "X-User-Roles", required = false) List<String> roles) {


        if (roles == null || roles.stream().noneMatch(r -> r.equalsIgnoreCase("ADMIN"))) {
            throw new ForbiddenException("Access denied");
        }

        detailFilters.remove("page_size");
        detailFilters.remove("page_number");

        AllProfilesPaginatedResponseDto allProfiles = adminProfileService.getAllProfiles(pageSize, pageNumber, detailFilters);

        return ResponseEntity.ok(allProfiles);
    }

}
