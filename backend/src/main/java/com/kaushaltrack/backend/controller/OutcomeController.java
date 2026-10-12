package com.kaushaltrack.backend.controller;

import com.kaushaltrack.backend.dto.OutcomeForm;
import com.kaushaltrack.backend.dto.OutcomeResponse;
import com.kaushaltrack.backend.service.OutcomeService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Admin side: see outcomes and enter them manually from the website. */
@RestController
@RequestMapping("/api/outcomes")
public class OutcomeController {

    private final OutcomeService service;

    public OutcomeController(OutcomeService service) {
        this.service = service;
    }

    @GetMapping
    public List<OutcomeResponse> list() {
        return service.findAllCompleted();
    }

    @PutMapping("/{traineeId}")
    public OutcomeResponse save(@PathVariable Long traineeId, @RequestBody OutcomeForm form) {
        return service.save(traineeId, form, "ADMIN");
    }
}
