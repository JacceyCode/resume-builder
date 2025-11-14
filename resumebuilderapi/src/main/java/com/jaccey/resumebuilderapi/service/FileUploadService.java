package com.jaccey.resumebuilderapi.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FileUploadService {
    private final Cloudinary cloudinary;

    public Map<String, String> uploadSingleImage(MultipartFile file) throws IOException {
        Map<String, Object> imageUploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                "resource_type", "image",
                        "folder", "resume_builder"
        ));


        return Map.of("imageUrl", imageUploadResult.get("secure_url").toString());
    }
}
