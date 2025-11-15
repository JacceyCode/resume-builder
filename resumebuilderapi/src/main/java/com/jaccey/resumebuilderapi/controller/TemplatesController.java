package com.jaccey.resumebuilderapi.controller;

import com.jaccey.resumebuilderapi.service.TemplatesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.jaccey.resumebuilderapi.util.AppConstants.AUTH_CONTROLLER;
import static com.jaccey.resumebuilderapi.util.AppConstants.TEMPLATES_CONTROLLER;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(TEMPLATES_CONTROLLER)
public class TemplatesController {
    private final TemplatesService templatesService;

    @GetMapping
    public ResponseEntity<?> getTemplates(Authentication authentication) {
        // Step 1: Call the service method
        Map<String, Object> response = templatesService.getTemplates(authentication.getPrincipal());

        // Return response
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
