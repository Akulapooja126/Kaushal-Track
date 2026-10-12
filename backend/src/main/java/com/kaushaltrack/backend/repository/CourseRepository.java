package com.kaushaltrack.backend.repository;

import com.kaushaltrack.backend.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    boolean existsByNameIgnoreCase(String name);
}
