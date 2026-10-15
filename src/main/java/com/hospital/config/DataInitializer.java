package com.hospital.config;

import com.hospital.entity.Department;
import com.hospital.entity.Doctor;
import com.hospital.entity.Patient;
import com.hospital.entity.User;
import com.hospital.repository.DepartmentRepository;
import com.hospital.repository.DoctorRepository;
import com.hospital.repository.PatientRepository;
import com.hospital.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Optional;

/**
 * Safe, idempotent database initializer.
 * Runs on application startup to ensure core departments, admin account,
 * doctor profiles, and demo patient accounts exist in the database without duplicates.
 * Can be disabled at runtime via APP_DB_INIT_ENABLED=false.
 */
@Component
@ConditionalOnProperty(name = "app.db.init.enabled", havingValue = "true", matchIfMissing = true)
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    public DataInitializer(DepartmentRepository departmentRepository,
                           UserRepository userRepository,
                           DoctorRepository doctorRepository,
                           PatientRepository patientRepository,
                           PasswordEncoder passwordEncoder,
                           org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Checking and initializing Smart Hospital database records...");
        modernizeAppointmentSlotIndex();
        initDepartments();
        initAdminUser();
        linkExistingDoctorUsers();
        initIndianDoctors();
        initDemoPatients();
        log.info("Smart Hospital database verification completed successfully.");
    }

    private void modernizeAppointmentSlotIndex() {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'appointments' AND index_name = 'unique_doc_slot'",
                    Integer.class
            );
            if (count != null && count > 0) {
                log.info("Modernizing appointments index: dropping legacy rigid unique_doc_slot index...");
                jdbcTemplate.execute("ALTER TABLE appointments DROP INDEX unique_doc_slot");
            }

            Integer activeCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'appointments' AND index_name = 'unique_active_doc_slot'",
                    Integer.class
            );
            if (activeCount == null || activeCount == 0) {
                log.info("Creating active slot unique index (unique_active_doc_slot) to support immediate slot release upon cancellation...");
                jdbcTemplate.execute("CREATE UNIQUE INDEX unique_active_doc_slot ON appointments (doctor_id, appointment_date, appointment_time, (CASE WHEN status != 'CANCELLED' THEN 1 ELSE NULL END))");
            }
        } catch (Exception e) {
            log.warn("Note on slot index modernization: {}", e.getMessage());
        }
    }

    private void initDepartments() {
        createDepartmentIfMissing("Cardiology", "Heart, blood vessels, and cardiovascular health management.", "FaHeartbeat");
        createDepartmentIfMissing("Neurology", "Brain, spinal cord, and nervous system specialists.", "FaBrain");
        createDepartmentIfMissing("Orthopedics", "Bones, joints, ligaments, tendons, and muscles care.", "FaBone");
        createDepartmentIfMissing("Pediatrics", "Comprehensive healthcare for infants, children, and adolescents.", "FaBaby");
        createDepartmentIfMissing("Dermatology", "Skin, hair, nails, and cosmetic healthcare treatments.", "FaUserMd");
        createDepartmentIfMissing("General Medicine", "Primary healthcare, diagnosis, and non-surgical treatments.", "FaStethoscope");
    }

    private void createDepartmentIfMissing(String name, String description, String icon) {
        if (!departmentRepository.existsByName(name)) {
            Department dept = new Department(name, description, icon);
            departmentRepository.save(dept);
            log.info("Created missing department: {}", name);
        }
    }

    private void initAdminUser() {
        if (!userRepository.existsByEmail("admin@hospital.com")) {
            User admin = new User(
                    "System Admin",
                    "admin@hospital.com",
                    passwordEncoder.encode("admin123"),
                    "ADMIN",
                    "9876543210"
            );
            userRepository.save(admin);
            log.info("Created missing admin user: admin@hospital.com");
        }
    }

    private void linkExistingDoctorUsers() {
        linkDoctorIfUnlinked(
                "robert.chen@hospital.com",
                "Cardiology",
                "Cardiologist",
                "MD, DM Cardiology",
                12,
                new BigDecimal("800.00"),
                "Monday, Tuesday, Wednesday, Thursday, Friday",
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                25
        );

        linkDoctorIfUnlinked(
                "sarah.jenkins@hospital.com",
                "Neurology",
                "Neurologist",
                "MD, MCh Neurology",
                15,
                new BigDecimal("900.00"),
                "Monday, Wednesday, Friday",
                LocalTime.of(10, 0),
                LocalTime.of(16, 0),
                20
        );

        linkDoctorIfUnlinked(
                "michael.vance@hospital.com",
                "Pediatrics",
                "Pediatrician",
                "MD Pediatrics",
                8,
                new BigDecimal("600.00"),
                "Monday, Tuesday, Wednesday, Thursday, Friday, Saturday",
                LocalTime.of(9, 0),
                LocalTime.of(15, 0),
                30
        );
    }

    private void linkDoctorIfUnlinked(String email, String departmentName, String specialization,
                                     String qualification, int experienceYears, BigDecimal fee,
                                     String days, LocalTime start, LocalTime end, int maxPatients) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (doctorRepository.findByUserId(user.getId()).isEmpty()) {
                Doctor doctor = new Doctor();
                doctor.setUser(user);
                departmentRepository.findByName(departmentName).ifPresent(doctor::setDepartment);
                doctor.setSpecialization(specialization);
                doctor.setQualification(qualification);
                doctor.setExperienceYears(experienceYears);
                doctor.setConsultationFee(fee);
                doctor.setAvailableDays(days);
                doctor.setAvailableTimeStart(start);
                doctor.setAvailableTimeEnd(end);
                doctor.setMaxPatientsPerDay(maxPatients);
                doctorRepository.save(doctor);
                log.info("Linked existing user {} to new doctor profile in department {}", email, departmentName);
            }
        }
    }

    private void initIndianDoctors() {
        seedDoctor(
                "Dr. Rajesh Sharma",
                "rajesh.sharma@hospital.com",
                "9876543220",
                "Cardiology",
                "Senior Cardiologist",
                "MD, DM (Cardiology), AIIMS",
                15,
                new BigDecimal("800.00"),
                "Monday, Tuesday, Wednesday, Thursday, Friday",
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                25
        );

        seedDoctor(
                "Dr. Priya Patel",
                "priya.patel@hospital.com",
                "9876543221",
                "Dermatology",
                "Consultant Dermatologist",
                "MD Dermatology, DNB",
                9,
                new BigDecimal("700.00"),
                "Monday, Tuesday, Wednesday, Thursday, Friday",
                LocalTime.of(10, 0),
                LocalTime.of(16, 0),
                20
        );

        seedDoctor(
                "Dr. Suresh Reddy",
                "suresh.reddy@hospital.com",
                "9876543222",
                "Orthopedics",
                "Orthopedic Surgeon",
                "MS (Ortho), MCh, NIMS Hyderabad",
                14,
                new BigDecimal("850.00"),
                "Monday, Tuesday, Wednesday, Thursday, Friday, Saturday",
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                30
        );

        seedDoctor(
                "Dr. Ananya Sen",
                "ananya.sen@hospital.com",
                "9876543223",
                "General Medicine",
                "General Physician",
                "MD General Medicine",
                10,
                new BigDecimal("500.00"),
                "Monday, Tuesday, Wednesday, Thursday, Friday, Saturday",
                LocalTime.of(9, 0),
                LocalTime.of(18, 0),
                35
        );
    }

    private void seedDoctor(String name, String email, String phone, String departmentName,
                            String specialization, String qualification, int experienceYears,
                            BigDecimal fee, String days, LocalTime start, LocalTime end, int maxPatients) {
        if (!userRepository.existsByEmail(email)) {
            User user = new User(name, email, passwordEncoder.encode("admin123"), "DOCTOR", phone);
            user = userRepository.save(user);

            Doctor doctor = new Doctor();
            doctor.setUser(user);
            departmentRepository.findByName(departmentName).ifPresent(doctor::setDepartment);
            doctor.setSpecialization(specialization);
            doctor.setQualification(qualification);
            doctor.setExperienceYears(experienceYears);
            doctor.setConsultationFee(fee);
            doctor.setAvailableDays(days);
            doctor.setAvailableTimeStart(start);
            doctor.setAvailableTimeEnd(end);
            doctor.setMaxPatientsPerDay(maxPatients);
            doctorRepository.save(doctor);
            log.info("Created new doctor: {} ({})", name, email);
        } else {
            User user = userRepository.findByEmail(email).orElse(null);
            if (user != null && doctorRepository.findByUserId(user.getId()).isEmpty()) {
                Doctor doctor = new Doctor();
                doctor.setUser(user);
                departmentRepository.findByName(departmentName).ifPresent(doctor::setDepartment);
                doctor.setSpecialization(specialization);
                doctor.setQualification(qualification);
                doctor.setExperienceYears(experienceYears);
                doctor.setConsultationFee(fee);
                doctor.setAvailableDays(days);
                doctor.setAvailableTimeStart(start);
                doctor.setAvailableTimeEnd(end);
                doctor.setMaxPatientsPerDay(maxPatients);
                doctorRepository.save(doctor);
                log.info("Linked existing user {} to doctor record", email);
            }
        }
    }

    private void initDemoPatients() {
        seedPatient(
                "John Doe",
                "john.doe@gmail.com",
                "9123456789",
                34,
                "Male",
                "O+",
                "Plot 42, Jubilee Hills, Hyderabad, Telangana 500033",
                "9123456700"
        );

        seedPatient(
                "Emily Watson",
                "emily.watson@gmail.com",
                "9123456790",
                28,
                "Female",
                "A+",
                "Flat 302, Madhapur, Hyderabad, Telangana 500081",
                "9123456701"
        );
    }

    private void seedPatient(String name, String email, String phone, int age, String gender,
                             String bloodGroup, String address, String emergencyContact) {
        User user;
        if (!userRepository.existsByEmail(email)) {
            user = new User(name, email, passwordEncoder.encode("admin123"), "PATIENT", phone);
            user = userRepository.save(user);
            log.info("Created missing demo patient user: {}", email);
        } else {
            user = userRepository.findByEmail(email).orElse(null);
        }

        if (user != null && patientRepository.findByUserId(user.getId()).isEmpty()) {
            Patient patient = new Patient();
            patient.setUser(user);
            patient.setAge(age);
            patient.setGender(gender);
            patient.setBloodGroup(bloodGroup);
            patient.setAddress(address);
            patient.setEmergencyContact(emergencyContact);
            patientRepository.save(patient);
            log.info("Created missing patient profile for: {}", email);
        }
    }
}
