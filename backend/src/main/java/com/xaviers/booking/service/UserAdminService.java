package com.xaviers.booking.service;

import com.xaviers.booking.dto.UserDto;
import com.xaviers.booking.model.Role;
import com.xaviers.booking.model.User;
import com.xaviers.booking.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserAdminService {
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public UserAdminService(UserRepository userRepo, PasswordEncoder passwordEncoder) { this.userRepo = userRepo; this.passwordEncoder = passwordEncoder; }

    public List<UserDto> listAll() {
        return userRepo.findAll().stream().map(u -> new UserDto(u.getId(), u.getEmail(), u.getName(), u.getRole().name())).toList();
    }

    public UserDto add(String email, String name, String roleStr, String password) {
        if (email == null || email.isBlank() || name == null || name.isBlank() || password == null || password.isBlank() || roleStr == null || roleStr.isBlank())
            throw new IllegalArgumentException("All fields are compulsory");
        if (!email.endsWith("@xaviers.edu.in")) throw new IllegalArgumentException("Email must end with @xaviers.edu.in");
        if (password.length() < 8) throw new IllegalArgumentException("Password must be at least 8 characters.");
        Role role = Role.valueOf(roleStr);
        if (role == Role.SUPERADMIN) throw new IllegalArgumentException("Cannot create SUPERADMIN via API");
        if (userRepo.findByEmail(email.toLowerCase()).isPresent()) throw new IllegalArgumentException("User already exists.");
        User u = new User();
        u.setEmail(email.toLowerCase().trim());
        u.setName(name.trim());
        u.setRole(role);
        u.setActive(true);
        u.setPassword(passwordEncoder.encode(password));
        u = userRepo.save(u);
        return new UserDto(u.getId(), u.getEmail(), u.getName(), u.getRole().name());
    }

    public void remove(Long id) {
        User u = userRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (u.getRole() == Role.SUPERADMIN) throw new IllegalArgumentException("Cannot remove superadmin");
        u.setActive(false);
        userRepo.save(u);
    }
}
