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
        if (email == null || name == null || role == null) return ResponseEntity.badRequest().body(Map.of("error", "email, name, role required"));
        return ResponseEntity.ok(adminService.add(email, name, role));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> remove(@PathVariable Long id) {
        adminService.remove(id);
        return ResponseEntity.ok(Map.of("message", "User deactivated"));
    }
}
