package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class RedshiftTestController {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public RedshiftTestController(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @GetMapping("/process-end")
    public Map<String, Object> getProcessInfo() {
        try {
            // Redshift-specific query
            String processEndDate = jdbcTemplate.queryForObject(
                    "SELECT process_end_date::DATE FROM \"dev\".\"public\".\"processing_date\";", 
                    String.class);
            
            LocalDateTime currentDateTime = LocalDateTime.now();
            
            return Map.of(
                "processEndDate", processEndDate,
                "currentDateTime", currentDateTime.toString(),
                "status", "success"
            );
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("status", "error");
            errorResult.put("error", e.getMessage());
            errorResult.put("errorType", e.getClass().getName());
            errorResult.put("currentDateTime", LocalDateTime.now().toString());
            return errorResult;
        }
    }
}
