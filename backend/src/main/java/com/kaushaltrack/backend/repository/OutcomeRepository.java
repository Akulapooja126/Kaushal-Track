package com.kaushaltrack.backend.repository;

import com.kaushaltrack.backend.model.Outcome;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface OutcomeRepository extends JpaRepository<Outcome, Long> {
    Optional<Outcome> findByTraineeId(Long traineeId);
}
