package com.kaushaltrack.backend.repository;

import com.kaushaltrack.backend.model.OutcomeInvite;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface InviteRepository extends JpaRepository<OutcomeInvite, Long> {
    Optional<OutcomeInvite> findByToken(String token);

    Optional<OutcomeInvite> findTopByTraineeIdOrderBySentAtDesc(Long traineeId);
}
