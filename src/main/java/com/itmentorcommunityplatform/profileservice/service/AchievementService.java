package com.itmentorcommunityplatform.profileservice.service;

import com.itmentorcommunityplatform.profileservice.config.AchievementConfig;
import com.itmentorcommunityplatform.profileservice.domain.Achievement;
import com.itmentorcommunityplatform.profileservice.domain.Profile;
import com.itmentorcommunityplatform.profileservice.domain.achievement.AchievementCriteriaChecker;
import com.itmentorcommunityplatform.profileservice.domain.achievement.AchievementStrategyRegistry;
import com.itmentorcommunityplatform.profileservice.domain.type.AchievementType;
import com.itmentorcommunityplatform.profileservice.dto.event.ProjectCreatedEvent;
import com.itmentorcommunityplatform.profileservice.dto.request.AchievementsVisibleRequestDto;
import com.itmentorcommunityplatform.profileservice.dto.response.AchievementResponseDto;
import com.itmentorcommunityplatform.profileservice.exception.ForbiddenException;
import com.itmentorcommunityplatform.profileservice.exception.NotFoundException;
import com.itmentorcommunityplatform.profileservice.repository.AchievementRepository;
import com.itmentorcommunityplatform.profileservice.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final AchievementStrategyRegistry registry;
    private final ProfileService profileService;
    private final TransactionTemplate transactionTemplate;
    private final ProfileRepository profileRepository;
    private final AchievementConfig achievementConfig;

    public void recheckAndAwardAchievements(ProjectCreatedEvent event) {
        profileService.getProfileForEvent(event).ifPresent(profile ->
                transactionTemplate.executeWithoutResult(status -> {
                    for (AchievementType type : AchievementType.values()) {
                        AchievementCriteriaChecker checker = registry.getStrategy(type);
                        if (checker != null && checker.checkCriteria(event.getAuthorTelegramUserId())) {
                            awardAchievement(event, type, profile);
                        }
                    }
                }));
    }

    public List<AchievementResponseDto> getProfileAchievements(Long telegramUserId) {

        Map<AchievementType, String> achievementsDescriptions = achievementConfig.getAchievements();

        Long profileId = profileRepository.findByTelegramUserId(telegramUserId)
                .map(Profile::getId)
                .orElseThrow(() -> new NotFoundException("Profile not found"));

        Map<AchievementType, AchievementResponseDto> profileAchievements = achievementRepository.findAchievemetsByProfileId(profileId)
                .stream()
                .map(achievement -> new AchievementResponseDto(
                        achievement.getAchievementType(),
                        achievement.getEarnedTimestamp(),
                        achievementsDescriptions.get(achievement.getAchievementType()),
                        achievement.isPubliclyVisible()
                ))
                .collect(Collectors.toMap(AchievementResponseDto::getType, Function.identity()));

        achievementsDescriptions.forEach((type, name) -> {
            AchievementResponseDto achievementResponseDto = new AchievementResponseDto(type, 0L, name, true);
            profileAchievements.putIfAbsent(type, achievementResponseDto);
        });

        return new ArrayList<>(profileAchievements.values());
    }

    @Transactional
    public AchievementResponseDto setAchievementPublicity(Long telegramUserId,
                                                          AchievementsVisibleRequestDto visibility,
                                                          AchievementType type) {

        Profile profile = profileService.getProfileByTelegramIdOrThrow(telegramUserId);

        Long profileId = profile.getId();

        Achievement achievement = getAchievementOrThrow(profileId, type);
        achievement.setPubliclyVisible(visibility.publiclyVisible());

        Achievement savedAchievement = achievementRepository.save(achievement);

        String description = achievementConfig.getAchievements().get(type);

        return new AchievementResponseDto(
                savedAchievement.getAchievementType(),
                savedAchievement.getEarnedTimestamp(),
                description,
                savedAchievement.isPubliclyVisible()
        );
    }

    private void awardAchievement(ProjectCreatedEvent event, AchievementType achievementType, Profile profile) {
        if (alreadyHasAchievement(profile.getId(), achievementType)) {
            log.info("Achievement issuance skipped: user (profileId: {}({})) already owns achievement of type: {}",
                    profile.getId(), event.getAuthorTelegramUserId(), achievementType);
            return;
        }

        Achievement achievement = Achievement
                .builder()
                .profileId(profile.getId())
                .achievementType(achievementType)
                .earnedTimestamp(System.currentTimeMillis())
                .publiclyVisible(true)
                .build();

        achievementRepository.save(achievement);
        log.info("User (profileId: {}({})), earned achievement: {}",
                profile.getId(), event.getAuthorTelegramUserId(), achievementType);
    }

    private Achievement getAchievementOrThrow(Long profileId, AchievementType type) {
        return achievementRepository
                .findByProfileIdAndAchievementType(profileId, type)
                .orElseThrow(() -> {
                    log.warn("User (profileId): {} tried to access achievement {} which they don't own", profileId, type);
                    return new ForbiddenException("User does not own the achievement");
                });
    }

    private boolean alreadyHasAchievement(Long profileId, AchievementType achievementType) {
        return achievementRepository.existsByProfileIdAndAchievementType(profileId, achievementType);
    }
}