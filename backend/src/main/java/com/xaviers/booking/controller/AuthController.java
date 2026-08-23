package com.xaviers.booking.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.xaviers.booking.dto.UserDto;
import com.xaviers.booking.model.User;
import com.xaviers.booking.repository.UserRepository;
import com.xaviers.booking.security.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepo;
    private final JwtUtil jwtUtil;
    @Value("${app.google.client-id:}")
    private String googleClientId;
    @Value("${app.oauth.allowed-domain:xaviers.edu.in}")
    private String allowedDomain;

    public AuthController(UserRepository userRepo, JwtUtil jwtUtil) {
        this.userRepo = userRepo; this.jwtUtil = jwtUtil;
    }

    private void setCookie(HttpServletResponse res, String token) {
        Cookie c = new Cookie("token", token);
        c.setHttpOnly(true);
        c.setPath("/");
        c.setMaxAge(86400);
        res.addCookie(c);
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> body, HttpServletResponse res) throws Exception {
        String idToken = body.get("idToken");
        if (idToken == null) return ResponseEntity.badRequest().body(Map.of("error", "idToken required"));
        String email;
        String name = null;
        if (googleClientId == null || googleClientId.isBlank() || googleClientId.startsWith("REPLACE")) {
            return ResponseEntity.status(500).body(Map.of("error", "Google OAuth not configured. Use /api/auth/dev-login for local dev."));
        }
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();
        GoogleIdToken token = verifier.verify(idToken);
        if (token == null) return ResponseEntity.status(401).body(Map.of("error", "Invalid Google token"));
        var payload = token.getPayload();
        if (!allowedDomain.equals(payload.getHostedDomain())) return ResponseEntity.status(403).body(Map.of("error", "Domain not allowed"));
        email = payload.getEmail();
        name = (String) payload.get("name");
        return loginWithEmail(email, name, res);
    }

    @PostMapping("/dev-login")
    public ResponseEntity<?> devLogin(@RequestBody Map<String, String> body, HttpServletResponse res) {
        String email = body.get("email");
        if (email == null) return ResponseEntity.badRequest().body(Map.of("error", "email required"));
        return loginWithEmail(email.toLowerCase(), null, res);
    }

    private ResponseEntity<?> loginWithEmail(String email, String googleName, HttpServletResponse res) {
        var opt = userRepo.findByEmail(email.toLowerCase());
        if (opt.isEmpty() || !opt.get().getActive()) return ResponseEntity.status(403).body(Map.of("error", "User not registered or inactive. Contact superadmin."));
        User user = opt.get();
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        setCookie(res, token);
        return ResponseEntity.ok(new UserDto(user.getId(), user.getEmail(), user.getName(), user.getRole().name()));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "Not logged in"));
        return ResponseEntity.ok(new UserDto(user.getId(), user.getEmail(), user.getName(), user.getRole().name()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse res) {
        Cookie c = new Cookie("token", "");
        c.setPath("/"); c.setMaxAge(0); c.setHttpOnly(true);
        res.addCookie(c);
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }
}
