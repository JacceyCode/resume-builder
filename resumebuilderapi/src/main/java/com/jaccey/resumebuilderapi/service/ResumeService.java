package com.jaccey.resumebuilderapi.service;

import com.jaccey.resumebuilderapi.document.Resume;
import com.jaccey.resumebuilderapi.document.User;
import com.jaccey.resumebuilderapi.dto.AuthResponse;
import com.jaccey.resumebuilderapi.dto.CreateResumeRequest;
import com.jaccey.resumebuilderapi.repository.ResumeRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeService {
    private final ResumeRepository resumeRepository;
    private final AuthService authService;
    private final FileUploadService fileUploadService;

    public Resume createResume(CreateResumeRequest request, Object principalObject) {
        // Step 1: Create resume object
        Resume newResume = new Resume();

        // Step 2: Get the current profile
        AuthResponse response = authService.getProfile(principalObject);

        // Step 3: Update the resume object
        newResume.setUserId(response.getId());
        newResume.setTitle(request.getTitle());

        // Step 4: Set default data for resume
        setDefaultResumeData(newResume);

        // Step 5: Save the resume data
        return resumeRepository.save(newResume);
    }

    private void setDefaultResumeData(Resume newResume) {
        newResume.setProfileInfo(new Resume.ProfileInfo());
        newResume.setContactInfo(new Resume.ContactInfo());
        newResume.setWorkExperience(new ArrayList<>());
        newResume.setEducation(new ArrayList<>());
        newResume.setSkills(new ArrayList<>());
        newResume.setProjects(new ArrayList<>());
        newResume.setCertifications(new ArrayList<>());
        newResume.setLanguages(new ArrayList<>());
        newResume.setInterests(new ArrayList<>());
    }

    public List<Resume> getUserResumes(Object principal) {
        // Step 1: Get the current profile
        AuthResponse response = authService.getProfile(principal);

        // Step 2: Call the repository method
        List<Resume> resumes = resumeRepository.findByUserIdOrderByUpdatedAtDesc(response.getId());

        // Step 3: Return response
        return resumes;
    }

    public Resume getUserResumeById(String id, Object principal) {
        // Step 1: Get the current profile
        AuthResponse response = authService.getProfile(principal);

        // Step 2: Call the repository method
        Resume resume = resumeRepository.findByUserIdAndId(response.getId(), id)
                .orElseThrow(() -> new RuntimeException("Resume not found"));

        // Step 3: Return response
        return resume;
    }

    public Resume updateResume(String resumeId, Resume updatedData, Object principal) {
        // Step 1: Get the current profile
        AuthResponse response = authService.getProfile(principal);

        // Step 2: Call the repository method
        Resume existingResume = resumeRepository.findByUserIdAndId(response.getId(), resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found"));

        // Step 3: Update the new data
        existingResume.setTitle(updatedData.getTitle());
        existingResume.setThumbnailLink(updatedData.getThumbnailLink());
        existingResume.setTemplate(updatedData.getTemplate());
        existingResume.setProfileInfo(updatedData.getProfileInfo());
        existingResume.setContactInfo(updatedData.getContactInfo());
        existingResume.setWorkExperience(updatedData.getWorkExperience());
        existingResume.setEducation(updatedData.getEducation());
        existingResume.setSkills(updatedData.getSkills());
        existingResume.setProjects(updatedData.getProjects());
        existingResume.setCertifications(updatedData.getCertifications());
        existingResume.setLanguages(updatedData.getLanguages());
        existingResume.setInterests(updatedData.getInterests());

        // Step 4: Save update to database
        resumeRepository.save(existingResume);

        // Step 5: Return updated data
        return existingResume;
    }

    public void deleteResume(String resumeId, Object principal) throws IOException {
        // Step 1: Get the current profile
        AuthResponse response = authService.getProfile(principal);

        // Step 2: Call the repository method
        Resume existingResume = resumeRepository.findByUserIdAndId(response.getId(), resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found"));

        // Step 3: Delete Resume from DB;
        resumeRepository.delete(existingResume);

        // Step 4: Delete uploaded images from cloudinary.
        if(Objects.nonNull(existingResume.getThumbnailLink())) {
            fileUploadService.deleteImageByUrl(existingResume.getThumbnailLink());
        }

        if(Objects.nonNull(existingResume.getProfileInfo().getProfilePreviewUrl())) {
            fileUploadService.deleteImageByUrl(existingResume.getProfileInfo().getProfilePreviewUrl());
        }
    }
}
