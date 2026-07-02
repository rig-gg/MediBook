package edu.cit.amihan.medibook.auth;

import edu.cit.amihan.medibook.auth.dto.AuthResponse;
import edu.cit.amihan.medibook.auth.dto.LoginRequest;
import edu.cit.amihan.medibook.auth.dto.RegisterRequest;
import edu.cit.amihan.medibook.doctor.entity.Doctor;
import edu.cit.amihan.medibook.doctor.repository.DoctorRepository;
import edu.cit.amihan.medibook.patient.entity.Patient;
import edu.cit.amihan.medibook.patient.repository.PatientRepository;
import edu.cit.amihan.medibook.security.JwtUtil;
import edu.cit.amihan.medibook.user.entity.Role;
import edu.cit.amihan.medibook.user.entity.User;
import edu.cit.amihan.medibook.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // Public login — works for ADMIN, STAFF, DOCTOR, PATIENT
    @PostMapping("/api/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(user);

        return ResponseEntity.ok(new AuthResponse(
                token, user.getUserId(), user.getUsername(), user.getFullName(), user.getRole()
        ));
    }

    // Public — open patient self-registration (BR-005), used by the Android app
    @PostMapping("/api/auth/register/patient")
    public ResponseEntity<?> registerPatient(@RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Username already taken.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email already registered.");
        }

        User user = User.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .role(Role.PATIENT)
                .build();

        User savedUser = userRepository.save(user);

        Patient patient = Patient.builder()
                .user(savedUser)
                .fullName(request.getFullName())
                .dateOfBirth(request.getDateOfBirth())
                .contactNumber(request.getContactNumber())
                .address(request.getAddress())
                .build();
        patientRepository.save(patient);

        String token = jwtUtil.generateToken(savedUser);

        return ResponseEntity.ok(new AuthResponse(
                token, savedUser.getUserId(), savedUser.getUsername(), savedUser.getFullName(), savedUser.getRole()
        ));
    }

    // Admin-only — provisions STAFF or DOCTOR accounts (BR-005)
    @PostMapping("/api/admin/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (request.getRole() == Role.ADMIN) {
            return ResponseEntity.badRequest().body("Cannot create additional ADMIN accounts via this endpoint.");
        }
        if (request.getRole() != Role.STAFF && request.getRole() != Role.DOCTOR) {
            return ResponseEntity.badRequest().body("Role must be STAFF or DOCTOR.");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Username already taken.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email already registered.");
        }

        User user = User.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .role(request.getRole())
                .build();

        User savedUser = userRepository.save(user);

        if (request.getRole() == Role.DOCTOR) {
            Doctor doctor = Doctor.builder()
                    .user(savedUser)
                    .fullName(request.getFullName())
                    .specialization(request.getSpecialization())
                    .contactNumber(request.getContactNumber())
                    .build();
            doctorRepository.save(doctor);
        }

        return ResponseEntity.ok("Account created successfully for " + savedUser.getUsername());
    }
}