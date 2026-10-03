package com.lowlifedev.webhook.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lowlifedev.webhook.model.Tenant;

import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    boolean existsBySlug(String slug);
}
