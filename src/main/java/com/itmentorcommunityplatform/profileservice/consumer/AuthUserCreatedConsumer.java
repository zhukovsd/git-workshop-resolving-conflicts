package com.itmentorcommunityplatform.profileservice.consumer;

import com.itmentorcommunityplatform.profileservice.dto.event.UserCreatedEvent;
import com.itmentorcommunityplatform.profileservice.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthUserCreatedConsumer {

    private final ProfileService profileService;

    @KafkaListener(topics = "${spring.kafka.topic.auth-user-created}", groupId = "profile-service-group")
    public void consumeUserCreatedEvent(UserCreatedEvent event) {
        log.info("Kafka Consumer: Received user created event: {}", event);
        try {
            profileService.createOrUpdateProfile(event);
            log.info("Kafka Consumer: Successfully processed event for user {}", event.getTelegramUserId());
        } catch (Exception e) {
            log.error("Kafka Consumer: Error processing event for user {}", event.getTelegramUserId(), e);
            throw e;
        }
    }
}