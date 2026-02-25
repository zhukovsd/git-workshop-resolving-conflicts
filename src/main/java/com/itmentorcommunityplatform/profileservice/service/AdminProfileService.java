package com.itmentorcommunityplatform.profileservice.service;

import com.itmentorcommunityplatform.profileservice.client.AuthServiceClient;
import com.itmentorcommunityplatform.profileservice.domain.Profile;
import com.itmentorcommunityplatform.profileservice.domain.ProfileDetail;
import com.itmentorcommunityplatform.profileservice.domain.type.Role;
import com.itmentorcommunityplatform.profileservice.dto.external.UserWithRolesResponseDto;
import com.itmentorcommunityplatform.profileservice.dto.response.AllProfilesPaginatedResponseDto;
import com.itmentorcommunityplatform.profileservice.dto.response.ProfileDetailsResponseDto;
import com.itmentorcommunityplatform.profileservice.dto.response.ProfileWithRolesResponseDto;
import com.itmentorcommunityplatform.profileservice.exception.InvalidPaginationException;
import com.itmentorcommunityplatform.profileservice.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminProfileService {

    private final ProfileRepository profileRepository;
    private final ProfileHelperService profileHelperService;
    private final AuthServiceClient authServiceClient;

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
            throw new InvalidPaginationException("Page number is greater than total page count");
        }

        try {
            return new AllProfilesPaginatedResponseDto(
                    allProfilesCount,
                    totalPageCount,
                    allProfilesPaginated.size(),
                    pageNumber,
                    enrichProfilesWithRoles(allProfilesPaginated)
                    );
        } catch (Exception ex) {
            log.error("All attempts to fetch user roles failed. Reason: {}", ex.getMessage());
            throw ex;
        }
    }

    private List<ProfileWithRolesResponseDto> enrichProfilesWithRoles(List<Profile> profiles) {
        if (profiles == null || profiles.isEmpty()) {
            return List.of();
        }

        List<Long> telegramIds = profiles.stream()
                .map(Profile::getTelegramUserId)
                .toList();

        List<UserWithRolesResponseDto> userRoles = authServiceClient.getAllUsers(telegramIds);

        Map<Long, List<Role>> rolesById = userRoles.stream()
                .collect(Collectors.toMap(
                        UserWithRolesResponseDto::telegramUserId,
                        user -> user.roles().stream()
                                .map(role -> Role.valueOf(role.toUpperCase()))
                                .toList()
                ));

        return profiles.stream()
                .map(profile -> new ProfileWithRolesResponseDto(
                        profile.getId(),
                        new ProfileDetailsResponseDto(profile.getDetails().stream()
                                .collect(Collectors.toMap(
                                        ProfileDetail::getDetailName,
                                        ProfileDetail::getDetailValue
                                ))),
                        rolesById.getOrDefault(profile.getTelegramUserId(), List.of())
                )).toList();
    }
}
