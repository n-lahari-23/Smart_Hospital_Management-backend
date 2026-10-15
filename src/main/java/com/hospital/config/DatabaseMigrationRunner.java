package com.hospital.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseMigrationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseMigrationRunner.class);
    private final JdbcTemplate jdbcTemplate;

    public DatabaseMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("Running unconditional database schema migrations...");
        try {
            jdbcTemplate.execute("ALTER TABLE appointments DROP INDEX unique_doc_slot");
            log.info("Successfully dropped legacy unique_doc_slot index.");
        } catch (Exception e) {
            log.info("Could not drop unique_doc_slot index: {}", e.getMessage());
        }

        try {
            jdbcTemplate.execute("ALTER TABLE appointments DROP CONSTRAINT unique_doc_slot");
            log.info("Successfully dropped legacy unique_doc_slot constraint.");
        } catch (Exception e) {
            log.info("Could not drop unique_doc_slot constraint: {}", e.getMessage());
        }

        try {
            jdbcTemplate.execute("CREATE UNIQUE INDEX unique_active_doc_slot ON appointments (doctor_id, appointment_date, appointment_time, (CASE WHEN status != 'CANCELLED' THEN 1 ELSE NULL END))");
            log.info("Successfully created unique_active_doc_slot expression index.");
        } catch (Exception e) {
            log.info("Could not create unique_active_doc_slot expression index: {}", e.getMessage());
        }
    }
}
