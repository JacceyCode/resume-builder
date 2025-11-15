package com.jaccey.resumebuilderapi.controller;

import com.jaccey.resumebuilderapi.dto.AuthResponse;
import com.jaccey.resumebuilderapi.dto.LoginRequest;
import com.jaccey.resumebuilderapi.dto.RegisterRequest;
import com.jaccey.resumebuilderapi.service.AuthService;
import com.jaccey.resumebuilderapi.service.FileUploadService;
import com.jaccey.resumebuilderapi.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import static com.jaccey.resumebuilderapi.util.AppConstants.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(AUTH_CONTROLLER)
public class AuthController {
    private final AuthService authService;
    private final FileUploadService fileUploadService;
    private final JwtUtil jwtUtil;

    @PostMapping(REGISTER)
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {

        AuthResponse response = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @GetMapping(VERIFY_EMAIL)
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Email verified successfully"));
    }

    @PostMapping(UPLOAD_PROFILE)
    public ResponseEntity<?> uploadImage(@RequestPart("image") MultipartFile file) throws IOException {
        Map<String, String> response = fileUploadService.uploadSingleImage(file);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping(LOGIN)
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);

        // Get Cookie
        ResponseCookie cookie = jwtUtil.generateCookie(response.getToken());

        return ResponseEntity.status(HttpStatus.OK).header(HttpHeaders.SET_COOKIE, cookie.toString()).body(response);
    }

    @PostMapping(RESEND_VERIFICATION)
    public ResponseEntity<?> resendVerification(@RequestBody Map<String, String> body) {
        String email = body.get("email");

        if(Objects.isNull(email)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
        }

        authService.resendVerification(email);

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("success", true, "message", "Verification email sent"));
    }
}
