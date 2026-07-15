package edu.cit.amihan.medibook.config;

import edu.cit.amihan.medibook.doctor.entity.Doctor;
import edu.cit.amihan.medibook.doctor.repository.DoctorRepository;
import edu.cit.amihan.medibook.user.entity.Role;
import edu.cit.amihan.medibook.user.entity.User;
import edu.cit.amihan.medibook.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String adminPassword = System.getenv("SEED_ADMIN_PASSWORD");
        String doctorPassword = System.getenv("SEED_DOCTOR_PASSWORD");
        String staffPassword = System.getenv("SEED_STAFF_PASSWORD");

        if (adminPassword == null || adminPassword.isBlank()) {
            adminPassword = "ChangeMe@1234";
        }
        if (doctorPassword == null || doctorPassword.isBlank()) {
            doctorPassword = "ChangeMe@1234";
        }
        if (staffPassword == null || staffPassword.isBlank()) {
            staffPassword = "ChangeMe@1234";
        }

        // Create admin
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .passwordHash(passwordEncoder.encode(adminPassword))
                    .email("admin@medibook.com")
                    .fullName("System Admin")
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(admin);
        }

        // Create doctor
        if (!userRepository.existsByUsername("doctor1")) {
            User doctorUser = User.builder()
                    .username("doctor1")
                    .passwordHash(passwordEncoder.encode(doctorPassword))
                    .email("doctor1@medibook.com")
                    .fullName("Dr. Juan Dela Cruz")
                    .role(Role.DOCTOR)
                    .build();
            User savedDoctor = userRepository.save(doctorUser);

            Doctor doctor = Doctor.builder()
                    .user(savedDoctor)
                    .fullName("Dr. Juan Dela Cruz")
                    .specialization("General Medicine")
                    .contactNumber("09171234567")
                    .build();
            doctorRepository.save(doctor);
        }

        // Create staff
        if (!userRepository.existsByUsername("staff1")) {
            User staffUser = User.builder()
                    .username("staff1")
                    .passwordHash(passwordEncoder.encode(staffPassword))
                    .email("staff1@medibook.com")
                    .fullName("Maria Santos")
                    .role(Role.STAFF)
                    .build();
            userRepository.save(staffUser);
        }
    }
}
