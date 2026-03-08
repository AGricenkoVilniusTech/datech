package com.datech.mvp.repository;

import com.datech.mvp.model.TimeEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TimeEntryRepository extends JpaRepository<TimeEntry, Long> {
    List<TimeEntry> findByProjectId(Long projectId);
}
