package com.kaushaltrack.backend.repository;

import com.kaushaltrack.backend.model.Trainee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TraineeRepository extends JpaRepository<Trainee, Long> {
}
