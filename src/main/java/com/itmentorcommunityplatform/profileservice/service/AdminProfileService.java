package com.itmentorcommunityplatform.profileservice.service;

import com.itmentorcommunityplatform.profileservice.domain.Profile;
import com.itmentorcommunityplatform.profileservice.dto.response.AllProfilesPaginatedResponseDto;
import com.itmentorcommunityplatform.profileservice.dto.response.ProfileWithRolesResponseDto;
import com.itmentorcommunityplatform.profileservice.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminProfileService {

    private final ProfileRepository profileRepository;
    private final ProfileHelperService profileHelperService;

    public AllProfilesPaginatedResponseDto getAllProfiles(int pageSize,
                                                          int pageNumber,
                                                          Map<String, String> detailFilters) {

        List<Profile> allProfilesPaginated;
        Long allProfilesCount;
        int totalPageCount;

        int offset = (pageNumber - 1) * pageSize;

        if (detailFilters == null || detailFilters.isEmpty()) {
            allProfilesPaginated = profileRepository.findAll(pageSize, offset);

            allProfilesCount = profileRepository.count();

        } else {
            profileHelperService.validateDetails(detailFilters);

            List<String[]> detailFiltersList = detailFilters.entrySet()
                    .stream().map(e -> new String[]{e.getKey().toLowerCase(), e.getValue().toLowerCase()})
                    .toList();

            allProfilesPaginated = profileRepository.findByDetails(detailFiltersList,
                    detailFiltersList.size(),
                    pageSize,
                    offset);

            allProfilesCount = profileRepository.countFiltered(detailFiltersList, detailFiltersList.size());
        }

        totalPageCount = Math.max((int) Math.ceil((double) allProfilesCount / pageSize), 1);

        if (pageNumber > totalPageCount) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page number is greater than total page count");
        }

        List<ProfileWithRolesResponseDto> items;

        try {
            items = profileHelperService.enrichProfilesWithRoles(allProfilesPaginated);
        } catch (Exception ex) {
            log.error("All attempts to fetch user roles failed. Reason: {}", ex.getMessage());
            throw ex;
        }
        return new AllProfilesPaginatedResponseDto(allProfilesCount, totalPageCount, allProfilesPaginated.size(), pageNumber, items);
    }
}
