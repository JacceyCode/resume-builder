package com.jaccey.resumebuilderapi.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.jaccey.resumebuilderapi.document.Resume;
import com.jaccey.resumebuilderapi.dto.AuthResponse;
import com.jaccey.resumebuilderapi.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FileUploadService {
    private final Cloudinary cloudinary;
    private final AuthService authService;
    private final ResumeRepository resumeRepository;

    public Map<String, String> uploadSingleImage(MultipartFile file) throws IOException {
        Map<String, Object> imageUploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                "resource_type", "image",
                        "folder", "resume_builder"
        ));


        return Map.of("imageUrl", imageUploadResult.get("secure_url").toString());
    }

    public Map<String, String> uploadResumeImages(String resumeId,
                                                  Object principal,
                                                  MultipartFile thumbnail,
                                                  MultipartFile profileImage) throws IOException {
        // Step 1: Get the current profile
        AuthResponse response = authService.getProfile(principal);

        // Step 2: Get the existing resume
        Resume existingResume = resumeRepository.findByUserIdAndId(response.getId(), resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found."));

        // Step 3: Upload the resume images and set the resume_url's
        Map<String, String> returnValue = new HashMap<>();
        Map<String, String> uploadResult;

        if(Objects.nonNull(thumbnail)) {
           uploadResult = uploadSingleImage(thumbnail);
            returnValue.put("thumbnailLink", uploadResult.get("imageUrl"));
            existingResume.setThumbnailLink(uploadResult.get("imageUrl"));
        }

        if(Objects.nonNull(profileImage)) {
            uploadResult = uploadSingleImage(profileImage);
            returnValue.put("profilePreviewUrl", uploadResult.get("imageUrl"));
            if(Objects.isNull(existingResume.getProfileInfo())) {
                existingResume.setProfileInfo(new Resume.ProfileInfo());
            }
            existingResume.getProfileInfo().setProfilePreviewUrl(uploadResult.get("imageUrl"));
        }

        // Step 4: Update the details into database
        resumeRepository.save(existingResume);
        returnValue.put("message", "Images uploaded successfully");

        // Step 5: Return the result
        return returnValue;
    }

    public void deleteImageByUrl(String imageUrl) throws IOException {
        // Extract public_id from the URL
        String publicId = extractPublicId(imageUrl);

        // Delete file from Cloudinary
        cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));
    }

    // Extract the public_id from a Cloudinary secure URL
    private String extractPublicId(String url) {
        try {
            URI uri = URI.create(url);
            String path = uri.getPath(); // /<cloud>/image/upload/v123/resume_builder/myimage.png
            String[] parts = path.split("/");

            // Everything AFTER the version number "v12345" is the public_id + extension
            String publicIdWithExtension = String.join("/", Arrays.copyOfRange(parts, 5, parts.length));

            // Remove the extension (.png, .jpg, etc)
            return publicIdWithExtension.replaceFirst("\\.[^.]+$", "");  // remove extension
        } catch (Exception e) {
            throw new RuntimeException("Invalid Cloudinary URL format: " + url, e);
        }
    }

}
