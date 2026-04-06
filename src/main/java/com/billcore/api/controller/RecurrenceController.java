package com.billcore.api.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recurrences")
public class RecurrenceController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("controller", "RecurrenceController", "status", "ok");
    }
}