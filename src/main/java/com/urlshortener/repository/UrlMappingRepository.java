package com.urlshortener.repository;


import com.urlshortener.model.UrlMapping;
import com.urlshortener.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {
    Optional<UrlMapping> findByShortCode(String shortCode);
    boolean existsByShortCode(String shortCode);
    List<UrlMapping> findByOwnerOrderByCreatedAtDesc(User owner);
    Optional<UrlMapping> findByIdAndOwner(Long id, User owner);
}