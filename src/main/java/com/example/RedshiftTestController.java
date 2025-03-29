package com.example;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedshiftTestController {

    private final JdbcTemplate jdbcTemplate;

    public RedshiftTestController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/current-date")
    public String getCurrentDate() {
        return jdbcTemplate.queryForObject("SELECT current_date;", String.class);
    }
}
