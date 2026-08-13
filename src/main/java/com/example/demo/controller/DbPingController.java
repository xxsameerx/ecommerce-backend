package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DbPingController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/db-ping")
    public Map<String, Object> dbPing() {
        Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        return Map.of(
            "status", "ok",
            "db", result
        );
    }
}