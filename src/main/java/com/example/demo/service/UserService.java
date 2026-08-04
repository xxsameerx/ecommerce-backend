package com.example.demo.service;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.model.Session;
import com.example.demo.model.User;
import com.example.demo.repository.SessionRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.JwtUtil;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SessionRepository sessionRepository;

    public String registerUser(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        if (userRepository.existsByMobileNumber(request.getMobileNumber())) {
            throw new RuntimeException("Mobile number already registered");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setMobileNumber(request.getMobileNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");

        userRepository.save(user);
        return "User registered successfully";
    }

    public AuthResponse loginUser(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmailOrMobile())
                .or(() -> userRepository.findByMobileNumber(request.getEmailOrMobile()))
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

        Session session = new Session();
        session.setUserId(user.getUserId());
        session.setJwtToken(token);
        session.setExpiryTime(LocalDateTime.now().plusHours(24));
        sessionRepository.save(session);

        return new AuthResponse(token, user.getRole(), user.getFullName());
    }
    public AuthResponse loginAdmin(LoginRequest request) {
    User user = userRepository.findByEmail(request.getEmailOrMobile())
            .or(() -> userRepository.findByMobileNumber(request.getEmailOrMobile()))
            .orElseThrow(() -> new RuntimeException("Invalid credentials"));

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
        throw new RuntimeException("Invalid credentials");
    }

    if (!"ADMIN".equals(user.getRole())) {
        throw new RuntimeException("Access denied: not an admin account");
    }

    String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

    Session session = new Session();
    session.setUserId(user.getUserId());
    session.setJwtToken(token);
    session.setExpiryTime(LocalDateTime.now().plusHours(24));
    sessionRepository.save(session);

    return new AuthResponse(token, user.getRole(), user.getFullName());
}

    public String logoutUser(String token) {
        Session session = sessionRepository.findByJwtTokenAndIsActiveTrue(token)
                .orElseThrow(() -> new RuntimeException("Session not found or already logged out"));
        session.setIsActive(false);
        sessionRepository.save(session);
        return "Logged out successfully";
    }

    public String generateResetToken(String emailOrMobile) {
        User user = userRepository.findByEmail(emailOrMobile)
                .or(() -> userRepository.findByMobileNumber(emailOrMobile))
                .orElseThrow(() -> new RuntimeException("No account found"));

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        return token;
    }

    public void resetPasswordWithToken(String token, String newPassword) {
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset token has expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }

    public String changePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return "Password changed successfully";
    }
}