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

    @GetMapping("/process-end")
    public String getCurrentDate() {
        return jdbcTemplate.queryForObject("SELECT process_end_date::DATE FROM \"dev\".\"public\".\"processing_date\";", String.class);
    }
}
