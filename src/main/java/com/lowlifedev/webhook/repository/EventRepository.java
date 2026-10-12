package com.lowlifedev.webhook.repository;

import com.lowlifedev.webhook.model.Event;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, UUID> {}
