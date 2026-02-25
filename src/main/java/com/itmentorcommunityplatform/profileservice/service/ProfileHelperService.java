package com.itmentorcommunityplatform.profileservice.service;

import com.itmentorcommunityplatform.profileservice.client.AuthServiceClient;
import com.itmentorcommunityplatform.profileservice.domain.Profile;
import com.itmentorcommunityplatform.profileservice.domain.ProfileDetail;
import com.itmentorcommunityplatform.profileservice.domain.type.ProfileDetailType;
import com.itmentorcommunityplatform.profileservice.domain.type.Role;
import com.itmentorcommunityplatform.profileservice.dto.external.UserWithRolesResponseDto;
import com.itmentorcommunityplatform.profileservice.dto.response.ProfileDetailsResponseDto;
import com.itmentorcommunityplatform.profileservice.dto.response.ProfileWithRolesResponseDto;
import com.itmentorcommunityplatform.profileservice.exception.BadRequestException;
import com.itmentorcommunityplatform.profileservice.validator.base.BaseProfileDetailValidator;
import com.itmentorcommunityplatform.profileservice.validator.registry.ProfileDetailValidatorRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileHelperService {

    private final AuthServiceClient authServiceClient;
    private final BaseProfileDetailValidator baseDetailValidator;
    private final ProfileDetailValidatorRegistry detailValidatorRegistry;

    public List<ProfileWithRolesResponseDto> enrichProfilesWithRoles(List<Profile> profiles) {
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

    public static Set<ProfileDetail> mergeProfileDetails(
            Set<ProfileDetail> existingDetails,
            Map<String, String> newDetailsMap
    ) {
        Map<String, String> mergedMap = new HashMap<>();

        existingDetails.forEach(detail ->
                mergedMap.put(detail.getDetailName(), detail.getDetailValue()));

        mergedMap.putAll(newDetailsMap);

        return mergedMap.entrySet().stream()
                .map(e -> new ProfileDetail(e.getKey(), e.getValue()))
                .collect(Collectors.toSet());
    }

    public void validateDetails(Map<String, String> details) {
        if (details == null || details.isEmpty()) {
            throw new BadRequestException("Profile details should not be empty");
        }

        details.forEach((detailName, detailValue) -> {
            ProfileDetailType type = ProfileDetailType.fromName(detailName)
                    .orElseThrow(() -> {
                        log.warn("Unknown detail name: {}", detailName);
                        return new BadRequestException("Unknown detail name");
                    });

            baseDetailValidator.validate(detailName, detailValue);

            detailValidatorRegistry.getSpecificValidator(type)
                    .ifPresent(v -> v.validate(detailValue));
        });
    }
}
