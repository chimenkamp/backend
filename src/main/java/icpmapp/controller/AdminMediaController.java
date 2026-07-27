package icpmapp.controller;

import icpmapp.dto.responses.ErrorResponse;
import icpmapp.dto.responses.MediaResponse;
import icpmapp.entities.User;
import icpmapp.repository.UserRepository;
import icpmapp.services.JWTService;
import icpmapp.services.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/media")
@RequiredArgsConstructor
public class AdminMediaController {
    
    private final MediaService mediaService;
    private final JWTService jwtService;
    private final UserRepository userRepository;
    
    /**
     * POST /api/v1/admin/media/upload
     * Uploads a file.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadMedia(
            @RequestHeader(value = "Authorization") String authorizationHeader,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "altText", required = false) String altText) {
        try {
            User currentUser = getCurrentUser(authorizationHeader);
            MediaResponse response = mediaService.uploadMedia(file, category, altText, currentUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage(), "UPLOAD_ERROR"));
        }
    }
    
    /**
     * GET /api/v1/admin/media
     * Lists all uploaded media.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping
    public ResponseEntity<List<MediaResponse>> getAllMedia(
            @RequestParam(value = "category", required = false) String category) {
        if (category != null && !category.isEmpty()) {
            return ResponseEntity.ok(mediaService.getMediaByCategory(category));
        }
        return ResponseEntity.ok(mediaService.getAllMedia());
    }
    
    /**
     * DELETE /api/v1/admin/media/{id}
     * Deletes a media file.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMedia(@PathVariable UUID id) {
        try {
            mediaService.deleteMedia(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage(), "NOT_FOUND"));
        }
    }
    
    // Helper method
    private User getCurrentUser(String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        String email = jwtService.extractUserName(token);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
