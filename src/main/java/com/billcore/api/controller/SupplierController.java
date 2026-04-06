package com.billcore.api.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/suppliers")
public class SupplierController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("controller", "SupplierController", "status", "ok");
    }
}