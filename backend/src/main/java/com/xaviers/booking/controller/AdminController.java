package com.xaviers.booking.controller;

import com.xaviers.booking.service.UserAdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('SUPERADMIN')")
public class AdminController {

    private final UserAdminService adminService;

    public AdminController(UserAdminService adminService) { this.adminService = adminService; }

    @GetMapping("/users")
    public ResponseEntity<?> list() { return ResponseEntity.ok(adminService.listAll()); }

    @PostMapping("/users")
    public ResponseEntity<?> add(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String name = body.get("name");
        String role = body.get("role");
        String password = body.get("password");
        if (email == null || email.isBlank() || name == null || name.isBlank() || role == null || role.isBlank() || password == null || password.isBlank())
            return ResponseEntity.badRequest().body(Map.of("error", "All fields are compulsory"));
        if (password.length() < 8) return ResponseEntity.badRequest().body(Map.of("error", "Password must be at least 8 characters."));
        if (role.isBlank()) return ResponseEntity.badRequest().body(Map.of("error", "Role must be selected."));
        return ResponseEntity.ok(adminService.add(email, name, role, password));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> remove(@PathVariable Long id) {
        adminService.remove(id);
        return ResponseEntity.ok(Map.of("message", "User deactivated"));
    }
}
