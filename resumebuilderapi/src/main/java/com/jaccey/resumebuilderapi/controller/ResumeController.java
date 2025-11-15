package com.jaccey.resumebuilderapi.controller;

import com.jaccey.resumebuilderapi.document.Resume;
import com.jaccey.resumebuilderapi.dto.CreateResumeRequest;
import com.jaccey.resumebuilderapi.service.FileUploadService;
import com.jaccey.resumebuilderapi.service.ResumeService;
import com.jaccey.resumebuilderapi.util.AppConstants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.jaccey.resumebuilderapi.util.AppConstants.ID;
import static com.jaccey.resumebuilderapi.util.AppConstants.UPLOAD_IMAGES;

@RestController
@RequestMapping(AppConstants.RESUME_CONTROLLER)
@RequiredArgsConstructor
@Slf4j
public class ResumeController {
    private final ResumeService resumeService;
    private final FileUploadService fileUploadService;

    @PostMapping
    public ResponseEntity<?> createResume(@Valid @RequestBody CreateResumeRequest request,
                                          Authentication authentication) {
        Resume newResume = resumeService.createResume(request, authentication.getPrincipal());

        return ResponseEntity.status(HttpStatus.CREATED).body(newResume);
    }

    @GetMapping
    public ResponseEntity<?> getUserResumes(Authentication authentication) {
        List<Resume> userResumes = resumeService.getUserResumes(authentication.getPrincipal());

        return ResponseEntity.status(HttpStatus.OK).body(userResumes);
    }

    @GetMapping(ID)
    public ResponseEntity<?> getResumeById(@PathVariable String id,
                                           Authentication authentication) {
        Resume userResume = resumeService.getUserResumeById(id, authentication.getPrincipal());

        return ResponseEntity.status(HttpStatus.OK).body(userResume);
    }

    @PutMapping(ID)
    public ResponseEntity<?> updateResume(@PathVariable String id,
                                          @RequestBody Resume updatedData,
                                          Authentication authentication) {
        Resume updatedResume = resumeService.updateResume(id, updatedData, authentication.getPrincipal());

        return ResponseEntity.status(HttpStatus.OK).body(updatedResume);
    }

    @PutMapping(UPLOAD_IMAGES)
    public ResponseEntity<?> uploadResumeImages(@PathVariable String id,
                                                @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
                                                @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
                                                Authentication authentication) throws IOException {
        Map<String, String> response = fileUploadService.uploadResumeImages(id, authentication.getPrincipal(), thumbnail, profileImage);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping(ID)
    public ResponseEntity<?> deleteResume(@PathVariable String id,
                                          Authentication authentication) throws IOException {
        resumeService.deleteResume(id, authentication.getPrincipal());

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Resume deleted successfully"));
    }
}
