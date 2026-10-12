package com.kaushaltrack.backend.controller;

import com.kaushaltrack.backend.model.Course;
import com.kaushaltrack.backend.repository.CourseRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseRepository courseRepo;

    public CourseController(CourseRepository courseRepo) {
        this.courseRepo = courseRepo;
    }

    @GetMapping
    public List<Course> list() {
        return courseRepo.findAll();
    }

    @PostMapping
    public Course create(@RequestBody Course course) {
        if (course.getName() == null || course.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Course name is required");
        }
        if (courseRepo.existsByNameIgnoreCase(course.getName().trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This course already exists");
        }
        course.setId(null);
        course.setName(course.getName().trim());
        return courseRepo.save(course);
    }
}
