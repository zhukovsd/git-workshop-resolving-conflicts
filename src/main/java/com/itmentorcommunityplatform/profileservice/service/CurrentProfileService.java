package com.itmentorcommunityplatform.profileservice.service;

import com.itmentorcommunityplatform.profileservice.domain.Achievement;
import com.itmentorcommunityplatform.profileservice.domain.Profile;
import com.itmentorcommunityplatform.profileservice.domain.ProfileDetail;
import com.itmentorcommunityplatform.profileservice.dto.request.ProfileUpdateRequestDto;
import com.itmentorcommunityplatform.profileservice.dto.response.ProfileWithAchievementsResponseDto;
import com.itmentorcommunityplatform.profileservice.mapper.ProfileMapper;
import com.itmentorcommunityplatform.profileservice.metrics.ProfileMetrics;
import com.itmentorcommunityplatform.profileservice.repository.AchievementRepository;
import com.itmentorcommunityplatform.profileservice.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.itmentorcommunityplatform.profileservice.service.ProfileHelperService.mergeProfileDetails;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrentProfileService {

    private final ProfileMetrics profileMetrics;
    private final ProfileRepository profileRepository;
    private final AchievementRepository achievementRepository;
    private final ProfileMapper profileMapper;
    private final ProfileHelperService profileHelperService;

    @Transactional
    public ProfileWithAchievementsResponseDto updateCurrentProfile(Long telegramUserId, ProfileUpdateRequestDto dto, String telegramUsername) {
        return profileMetrics.getGetProfileTimer().record(() -> {
            try {
                Map<String, String> newDetailsMap = dto.getDetails();
                profileHelperService.validateDetails(newDetailsMap);

                if (telegramUsername != null && !telegramUsername.isBlank()) {
                    newDetailsMap.put("telegram_url", "https://t.me/" + telegramUsername);
                }

                Profile profile = profileRepository.findByTelegramUserId(telegramUserId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

                List<Achievement> achievements = achievementRepository.findAllByProfileIdAndPubliclyVisibleTrue(profile.getId());

                Set<ProfileDetail> mergedDetails = mergeProfileDetails(profile.getDetails(), newDetailsMap);
                profile.setDetails(mergedDetails);

                profileRepository.save(profile);
                profileMetrics.getGetProfileSuccessCounter().increment();

                return profileMapper.mapToProfileWithAchievementsDto(profile.getDetails(), achievements);

            } catch (Exception e) {
                profileMetrics.getGetProfileErrorCounter().increment();
                throw e;
            }
        });
    }

    public ProfileWithAchievementsResponseDto getCurrentUserProfile(Long telegramUserId) {
        return profileMetrics.getGetProfileTimer().record(() -> {
            log.info("Fetching profile for telegramUserId: {}", telegramUserId);
            try {
                Profile profile = profileRepository.findByTelegramUserId(telegramUserId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

                List<Achievement> achievements = achievementRepository.findAllByProfileIdAndPubliclyVisibleTrue(profile.getId());

                profileMetrics.getGetProfileSuccessCounter().increment();

                return profileMapper.mapToProfileWithAchievementsDto(profile.getDetails(), achievements);

            } catch (Exception e) {
                profileMetrics.getGetProfileErrorCounter().increment();
                throw e;
            }
        });
    }

    public ProfileWithAchievementsResponseDto getUserProfile(Long profileId) {

        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Profile with id: %s not found".formatted(profileId)
                ));

        List<Achievement> achievements = achievementRepository.findAllByProfileIdAndPubliclyVisibleTrue(profileId);

        return profileMapper.mapToProfileWithAchievementsDto(profile.getDetails(), achievements);
    }
}
