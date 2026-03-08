package com.datech.mvp.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CrudService {
    public <T> List<T> findAll(JpaRepository<T, Long> repository) {
        return repository.findAll();
    }

    public <T> T save(JpaRepository<T, Long> repository, T entity) {
        return repository.save(entity);
    }

    public <T> T findById(JpaRepository<T, Long> repository, Long id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Entity not found: " + id));
    }

    public <T> void delete(JpaRepository<T, Long> repository, Long id) {
        repository.deleteById(id);
    }
}
