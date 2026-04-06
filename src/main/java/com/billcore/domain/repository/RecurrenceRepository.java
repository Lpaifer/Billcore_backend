package com.billcore.domain.repository;

import com.billcore.domain.entity.Recurrence;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecurrenceRepository extends JpaRepository<Recurrence, UUID> {
}
