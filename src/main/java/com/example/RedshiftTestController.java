package com.example;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class RedshiftTestController {

    private final JdbcTemplate jdbcTemplate;

    public RedshiftTestController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/process-end")
    public Map<String, Object> getProcessInfo() {
        String processEndDate = jdbcTemplate.queryForObject(
                "SELECT process_end_date::DATE FROM \"dev\".\"public\".\"processing_date\";",
                String.class
        );

        LocalDateTime currentDateTime = LocalDateTime.now();

        return Map.of(
                "processEndDate", processEndDate,
                "currentDateTime", currentDateTime.toString()
        );
    }
}
