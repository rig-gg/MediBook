package edu.cit.amihan.medibook.auth;

import edu.cit.amihan.medibook.auth.dto.AuthResponse;
import edu.cit.amihan.medibook.auth.dto.LoginRequest;
import edu.cit.amihan.medibook.auth.dto.LogoutRequest;
import edu.cit.amihan.medibook.auth.dto.RefreshTokenRequest;
import edu.cit.amihan.medibook.auth.dto.RegisterRequest;
import edu.cit.amihan.medibook.clinicstaff.entity.ClinicStaff;
import edu.cit.amihan.medibook.clinicstaff.repository.ClinicStaffRepository;
import edu.cit.amihan.medibook.doctor.entity.Doctor;
import edu.cit.amihan.medibook.doctor.repository.DoctorRepository;
import edu.cit.amihan.medibook.patient.entity.Patient;
import edu.cit.amihan.medibook.patient.repository.PatientRepository;
import edu.cit.amihan.medibook.security.JwtUtil;
import edu.cit.amihan.medibook.security.RateLimitService;
import edu.cit.amihan.medibook.security.TokenBlacklistService;
import edu.cit.amihan.medibook.user.entity.Role;
import edu.cit.amihan.medibook.user.entity.User;
import edu.cit.amihan.medibook.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final ClinicStaffRepository clinicStaffRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistService blacklistService;
    private final RateLimitService rateLimitService;

    private static final String LOGIN_RATE_LIMIT_KEY = "login";

    @PostMapping("/api/auth/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request,
                                   HttpServletRequest httpRequest) {
        String rateLimitKey = LOGIN_RATE_LIMIT_KEY + ":" + getClientIp(httpRequest);
        if (rateLimitService.isBlocked(rateLimitKey)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too many login attempts. Please try again in 1 minute.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (AuthenticationException e) {
            rateLimitService.recordAttempt(rateLimitKey);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password.");
        }

        rateLimitService.reset(rateLimitKey);

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        return ResponseEntity.ok(new AuthResponse(
                accessToken, refreshToken,
                user.getUserId(), user.getUsername(), user.getFullName(), user.getRole()
        ));
    }

    @PostMapping("/api/auth/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtUtil.isTokenValid(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token.");
        }

        String jti = jwtUtil.extractJti(refreshToken);
        if (blacklistService.isBlacklisted(jti)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token has been revoked.");
        }

        String username = jwtUtil.extractUsername(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        String newAccessToken = jwtUtil.generateAccessToken(userDetails);
        String newRefreshToken = jwtUtil.generateRefreshToken(userDetails);

        blacklistService.blacklist(jti);

        return ResponseEntity.ok(new AuthResponse(
                newAccessToken, newRefreshToken,
                null, null, null, null
        ));
    }

    @PostMapping("/api/auth/logout")
    public ResponseEntity<?> logout(@RequestBody(required = false) LogoutRequest request,
                                    @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (request != null && request.getRefreshToken() != null) {
            String jti = jwtUtil.extractJti(request.getRefreshToken());
            blacklistService.blacklist(jti);
        }

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            try {
                String jti = jwtUtil.extractJti(accessToken);
                blacklistService.blacklist(jti);
            } catch (Exception ignored) {
            }
        }

        return ResponseEntity.ok("Logged out successfully.");
    }

    @PostMapping("/api/auth/register/patient")
    public ResponseEntity<?> registerPatient(@Valid @RequestBody RegisterRequest request,
                                             HttpServletRequest httpRequest) {
        String rateLimitKey = "register:" + getClientIp(httpRequest);
        if (rateLimitService.isBlocked(rateLimitKey)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too many registration attempts. Please try again in 1 minute.");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            rateLimitService.recordAttempt(rateLimitKey);
            return ResponseEntity.badRequest().body("Username already taken.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            rateLimitService.recordAttempt(rateLimitKey);
            return ResponseEntity.badRequest().body("Email already registered.");
        }

        rateLimitService.reset(rateLimitKey);

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

        String accessToken = jwtUtil.generateAccessToken(savedUser);
        String refreshToken = jwtUtil.generateRefreshToken(savedUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(
                accessToken, refreshToken,
                savedUser.getUserId(), savedUser.getUsername(), savedUser.getFullName(), savedUser.getRole()
        ));
    }

    @PostMapping("/api/admin/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
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
        } else if (request.getRole() == Role.STAFF) {
            ClinicStaff staff = ClinicStaff.builder()
                    .user(savedUser)
                    .fullName(request.getFullName())
                    .contactNumber(request.getContactNumber())
                    .position(request.getPosition())
                    .build();
            clinicStaffRepository.save(staff);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body("Account created successfully for " + savedUser.getUsername());
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
