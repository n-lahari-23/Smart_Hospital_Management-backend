package com.hospital.controller;

import com.hospital.dto.AdminAnalyticsDTO;
import com.hospital.dto.DashboardStatsDTO;
import com.hospital.dto.PatientDTO;
import com.hospital.entity.User;
import com.hospital.repository.UserRepository;
import com.hospital.service.PatientService;
import com.hospital.service.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final ReportService reportService;
    private final UserRepository userRepository;
    private final PatientService patientService;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    public AdminController(ReportService reportService, UserRepository userRepository, PatientService patientService, org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
        this.reportService = reportService;
        this.userRepository = userRepository;
        this.patientService = patientService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/upgrade-schema")
    public ResponseEntity<java.util.Map<String, Object>> upgradeSchema() {
        java.util.Map<String, Object> res = new java.util.LinkedHashMap<>();
        try {
            jdbcTemplate.execute("ALTER TABLE appointments DROP INDEX unique_doc_slot");
            res.put("dropIndex", "SUCCESS");
        } catch (Exception e) {
            res.put("dropIndexError", e.getMessage());
        }
        try {
            jdbcTemplate.execute("ALTER TABLE appointments DROP CONSTRAINT unique_doc_slot");
            res.put("dropConstraint", "SUCCESS");
        } catch (Exception e) {
            res.put("dropConstraintError", e.getMessage());
        }
        try {
            jdbcTemplate.execute("CREATE UNIQUE INDEX unique_active_doc_slot ON appointments (doctor_id, appointment_date, appointment_time, (CASE WHEN status != 'CANCELLED' THEN 1 ELSE NULL END))");
            res.put("createActiveIndex", "SUCCESS");
        } catch (Exception e) {
            res.put("createActiveIndexError", e.getMessage());
        }
        try {
            res.put("indexes", jdbcTemplate.queryForList("SHOW INDEX FROM appointments WHERE Key_name LIKE '%doc_slot%'"));
        } catch (Exception e) {
            res.put("showIndexError", e.getMessage());
        }
        return ResponseEntity.ok(res);
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getStats() {
        return ResponseEntity.ok(reportService.getAdminStats());
    }

    @GetMapping("/analytics")
    public ResponseEntity<AdminAnalyticsDTO> getAnalytics(@RequestParam(defaultValue = "14") int days) {
        return ResponseEntity.ok(reportService.getAdminAnalytics(days));
    }

    @GetMapping("/appointments/export-csv")
    public void exportAppointmentsCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"appointments_report.csv\"");
        reportService.exportAppointmentsCsv(response.getWriter());
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/patients")
    public ResponseEntity<List<PatientDTO>> getAllPatients() {
        return ResponseEntity.ok(patientService.getAllPatients());
    }
}
