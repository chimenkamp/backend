package icpmapp.services.impl;

import icpmapp.dto.responses.MediaResponse;
import icpmapp.dto.responses.MediaFileResponse;
import icpmapp.entities.Media;
import icpmapp.entities.User;
import icpmapp.repository.MediaRepository;
import icpmapp.services.MediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaServiceImpl implements MediaService {

    private static final Pattern SAFE_EXTENSION = Pattern.compile("\\.[A-Za-z0-9]{1,10}$");
    
    private final MediaRepository mediaRepository;
    
    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    
    @Override
    @Transactional
    public MediaResponse uploadMedia(MultipartFile file, String category, String altText, User uploadedBy) {
        try {
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = safeExtension(originalFilename);
            String uniqueFilename = UUID.randomUUID().toString() + extension;
            
            // Ensure upload directory exists
            Path uploadPath = Paths.get(uploadDir, "media");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Save file
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Create media entity
            String url = baseUrl + "/api/v1/media/" + uniqueFilename;
            Media media = new Media();
            media.setFilename(uniqueFilename);
            media.setOriginalFilename(originalFilename != null ? originalFilename : uniqueFilename);
            media.setMimeType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
            media.setFileSize(file.getSize());
            media.setUrl(url);
            media.setCategory(category);
            media.setAltText(altText);
            media.setUploadedBy(uploadedBy);
            
            media = mediaRepository.save(media);
            
            return toResponse(media);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<MediaResponse> getAllMedia() {
        return mediaRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<MediaResponse> getMediaByCategory(String category) {
        return mediaRepository.findByCategory(category).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public MediaFileResponse getMediaFile(String filename) {
        Media media = mediaRepository.findByFilename(filename)
                .orElseThrow(() -> new RuntimeException("Media not found"));
        try {
            Path mediaDirectory = Paths.get(uploadDir, "media").toAbsolutePath().normalize();
            Path filePath = mediaDirectory.resolve(media.getFilename()).normalize();
            if (!filePath.startsWith(mediaDirectory)) {
                throw new RuntimeException("Invalid media path");
            }
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException("Media file not found");
            }
            return new MediaFileResponse(resource, media.getMimeType());
        } catch (IOException exception) {
            throw new RuntimeException("Failed to read media file", exception);
        }
    }
    
    @Override
    @Transactional
    public void deleteMedia(UUID id) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Media not found"));
        
        // Delete file from disk
        try {
            Path filePath = Paths.get(uploadDir, "media", media.getFilename());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("Failed to delete media file {}", media.getFilename(), e);
        }
        
        mediaRepository.delete(media);
    }
    
    // Helper method
    private MediaResponse toResponse(Media media) {
        return MediaResponse.builder()
                .id(media.getId())
                .url(media.getUrl())
                .filename(media.getFilename())
                .originalFilename(media.getOriginalFilename())
                .mimeType(media.getMimeType())
                .fileSize(media.getFileSize())
                .category(media.getCategory())
                .altText(media.getAltText())
                .createdAt(media.getCreatedAt())
                .build();
    }

    private String safeExtension(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }
        Matcher matcher = SAFE_EXTENSION.matcher(originalFilename);
        return matcher.find() ? matcher.group() : "";
    }
}
