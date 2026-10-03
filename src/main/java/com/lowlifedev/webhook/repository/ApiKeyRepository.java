package com.lowlifedev.webhook.repository;

import com.lowlifedev.webhook.model.ApiKey;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {
    List<ApiKey> findAllByTenantIdOrderByCreatedAtDesc(UUID tenantId);

    Optional<ApiKey> findByTenantIdAndId(UUID tenantId, UUID id);

    Optional<ApiKey> findByKeyHash(String keyHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query(
        "select k from ApiKey k where k.id = :id"
    )
    Optional<ApiKey> findByIdForUpdate(UUID id);
}
