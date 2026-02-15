package com.itmentorcommunityplatform.profileservice.repository;

import com.itmentorcommunityplatform.profileservice.domain.Project;
import com.itmentorcommunityplatform.profileservice.domain.type.RoadmapProjectType;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface ProjectRepository extends CrudRepository<Project, Long> {

    List<Project> findByAuthorTelegramUserId(Long authorTelegramUserId);

    @Query("""
           SELECT DISTINCT p.roadmap_project 
           FROM Project p  
           WHERE p.author_telegram_user_id = :userId
           """)
    Set<RoadmapProjectType> findProjectsNamesByUserId(@Param("userId") Long userId);

    @Query("""
           SELECT DISTINCT p.programming_language FROM Project p 
           WHERE p.author_telegram_user_id = :userId
           """)
    Set<String> findUsersProjectsLanguagesByUserId(@Param("userId") Long userId);

}
