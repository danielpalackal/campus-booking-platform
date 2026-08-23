package com.xaviers.booking.service;

import com.xaviers.booking.dto.UserDto;
import com.xaviers.booking.model.Role;
import com.xaviers.booking.model.User;
import com.xaviers.booking.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserAdminService {
    private final UserRepository userRepo;

    public UserAdminService(UserRepository userRepo) { this.userRepo = userRepo; }

    public List<UserDto> listAll() {
        return userRepo.findAll().stream().map(u -> new UserDto(u.getId(), u.getEmail(), u.getName(), u.getRole().name())).toList();
    }

    public UserDto add(String email, String name, String roleStr) {
        if (!email.endsWith("@xaviers.edu.in")) throw new IllegalArgumentException("Email must end with @xaviers.edu.in");
        Role role = Role.valueOf(roleStr);
        if (role == Role.SUPERADMIN) throw new IllegalArgumentException("Cannot create SUPERADMIN via API");
        User u = new User();
        u.setEmail(email.toLowerCase());
        u.setName(name);
        u.setRole(role);
        u.setActive(true);
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
