package com.kaushaltrack.backend.controller;

import com.kaushaltrack.backend.dto.TraineeRequest;
import com.kaushaltrack.backend.dto.TraineeResponse;
import com.kaushaltrack.backend.service.TraineeService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trainees")
public class TraineeController {

    private final TraineeService service;

    public TraineeController(TraineeService service) {
        this.service = service;
    }

    @GetMapping
    public List<TraineeResponse> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public TraineeResponse get(@PathVariable Long id) {
        return service.findOne(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TraineeResponse create(@Valid @RequestBody TraineeRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public TraineeResponse update(@PathVariable Long id, @Valid @RequestBody TraineeRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PostMapping("/{id}/send-outcome-email")
    public Map<String, Object> resend(@PathVariable Long id) {
        boolean sent = service.resendEmail(id);
        return Map.of("sent", sent,
                "message", sent ? "Email sent" : "Email could not be sent. Check the mail settings.");
    }
}
