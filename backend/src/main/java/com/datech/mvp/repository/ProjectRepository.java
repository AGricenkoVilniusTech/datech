package com.datech.mvp.repository;

import com.datech.mvp.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByClientId(Long clientId);

    Optional<Project> findByClientIdAndNameIgnoreCase(Long clientId, String name);
}
