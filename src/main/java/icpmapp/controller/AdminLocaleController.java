package icpmapp.controller;

import icpmapp.dto.requests.LocalizedContentBulkRequest;
import icpmapp.dto.requests.LocalizedContentRequest;
import icpmapp.dto.responses.ErrorResponse;
import icpmapp.dto.responses.LanguageSummaryResponse;
import icpmapp.entities.User;
import icpmapp.repository.UserRepository;
import icpmapp.services.JWTService;
import icpmapp.services.LocaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/locales")
@RequiredArgsConstructor
public class AdminLocaleController {
    
    private final LocaleService localeService;
    private final JWTService jwtService;
    private final UserRepository userRepository;
    
    /**
     * GET /api/v1/admin/locales
     * Returns all languages and their content counts.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping
    public ResponseEntity<List<LanguageSummaryResponse>> getAllLanguages() {
        return ResponseEntity.ok(localeService.getAllLanguages());
    }
    
    /**
     * GET /api/v1/admin/locales/{languageCode}
     * Returns all content for a language.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/{languageCode}")
    public ResponseEntity<Map<String, String>> getLanguageContent(@PathVariable String languageCode) {
        return ResponseEntity.ok(localeService.getLanguageContent(languageCode));
    }
    
    /**
     * PUT /api/v1/admin/locales/{languageCode}/{contentKey}
     * Updates a localized content item.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{languageCode}/{contentKey}")
    public ResponseEntity<?> updateContent(
            @PathVariable String languageCode,
            @PathVariable String contentKey,
            @RequestHeader(value = "Authorization") String authorizationHeader,
            @RequestBody LocalizedContentRequest request) {
        try {
            User currentUser = getCurrentUser(authorizationHeader);
            localeService.updateContent(languageCode, contentKey, request, currentUser);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage(), "VALIDATION_ERROR"));
        }
    }
    
    /**
     * POST /api/v1/admin/locales/{languageCode}/bulk
     * Bulk update localized content.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/{languageCode}/bulk")
    public ResponseEntity<?> bulkUpdateContent(
            @PathVariable String languageCode,
            @RequestHeader(value = "Authorization") String authorizationHeader,
            @RequestBody LocalizedContentBulkRequest request) {
        try {
            User currentUser = getCurrentUser(authorizationHeader);
            localeService.bulkUpdateContent(languageCode, request, currentUser);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage(), "VALIDATION_ERROR"));
        }
    }
    
    /**
     * DELETE /api/v1/admin/locales/{languageCode}/{contentKey}
     * Deletes a localized content override.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{languageCode}/{contentKey}")
    public ResponseEntity<?> deleteContent(
            @PathVariable String languageCode,
            @PathVariable String contentKey) {
        try {
            localeService.deleteContent(languageCode, contentKey);
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
