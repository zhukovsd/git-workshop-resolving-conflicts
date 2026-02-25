package com.itmentorcommunityplatform.profileservice.consumer;

import com.itmentorcommunityplatform.profileservice.dto.event.ProjectCreatedEvent;
import com.itmentorcommunityplatform.profileservice.service.AchievementService;
import com.itmentorcommunityplatform.profileservice.service.ProfileDetailService;
import com.itmentorcommunityplatform.profileservice.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectCreatedConsumer {

    private final ProjectService projectService;
    private final AchievementService achievementService;
    private final ProfileDetailService profileDetailService;

    @KafkaListener(topics = "${spring.kafka.topic.projects-project-created}", groupId = "profile-service-cg")
    public void consumeProjectCreatedEvent(ProjectCreatedEvent event) {
        log.info("ProjectCreated received: userId={}", event.getAuthorTelegramUserId());
        log.debug("ProjectCreated payload: userId={}", event);

        try {
            profileDetailService.upsertGithubProfileUrl(event.getAuthorTelegramUserId(), event.getGithubRepositoryUrl());
            projectService.createdProject(event);
            achievementService.recheckAndAwardAchievements(event);
            log.info("Kafka Consumer: Successfully processed event for user {}", event.getAuthorTelegramUserId());
        } catch (Exception e) {
            log.error("Kafka Consumer: Error processing event for user: {}",
                    event.getAuthorTelegramUserId(), e);
        }
    }
}
