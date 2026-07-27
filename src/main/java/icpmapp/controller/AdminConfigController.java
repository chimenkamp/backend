package icpmapp.controller;

import icpmapp.dto.requests.SiteConfigBulkRequest;
import icpmapp.dto.requests.SiteConfigCreateRequest;
import icpmapp.dto.requests.SiteConfigRequest;
import icpmapp.dto.responses.ErrorResponse;
import icpmapp.dto.responses.SiteConfigDetailResponse;
import icpmapp.entities.SiteConfig;
import icpmapp.entities.User;
import icpmapp.repository.UserRepository;
import icpmapp.services.ConfigService;
import icpmapp.services.JWTService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/config")
@RequiredArgsConstructor
public class AdminConfigController {
    
    private final ConfigService configService;
    private final JWTService jwtService;
    private final UserRepository userRepository;
    
    /**
     * GET /api/v1/admin/config
     * Returns all configuration (including non-public).
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping
    public ResponseEntity<List<SiteConfigDetailResponse>> getAllConfigs() {
        return ResponseEntity.ok(configService.getAllConfigs());
    }
    
    /**
     * GET /api/v1/admin/config/{key}
     * Returns single config item.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/{key}")
    public ResponseEntity<?> getConfigByKey(@PathVariable String key) {
        return configService.getConfigByKey(key)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Config not found", "NOT_FOUND")));
    }
    
    /**
     * PUT /api/v1/admin/config/{key}
     * Updates a config value.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{key}")
    public ResponseEntity<?> updateConfig(
            @PathVariable String key,
            @RequestHeader(value = "Authorization") String authorizationHeader,
            @RequestBody SiteConfigRequest request) {
        try {
            User currentUser = getCurrentUser(authorizationHeader);
            SiteConfig config = configService.updateConfig(key, request, currentUser);
            return ResponseEntity.ok(configService.getConfigByKey(config.getConfigKey()).orElse(null));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse(e.getMessage(), "NOT_FOUND"));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage(), "VALIDATION_ERROR"));
        }
    }
    
    /**
     * POST /api/v1/admin/config
     * Creates a new config entry.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping
    public ResponseEntity<?> createConfig(
            @RequestHeader(value = "Authorization") String authorizationHeader,
            @RequestBody SiteConfigCreateRequest request) {
        try {
            User currentUser = getCurrentUser(authorizationHeader);
            SiteConfig config = configService.createConfig(request, currentUser);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(configService.getConfigByKey(config.getConfigKey()).orElse(null));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ErrorResponse(e.getMessage(), "DUPLICATE_KEY"));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage(), "VALIDATION_ERROR"));
        }
    }
    
    /**
     * DELETE /api/v1/admin/config/{key}
     * Deletes a config entry.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{key}")
    public ResponseEntity<?> deleteConfig(@PathVariable String key) {
        try {
            configService.deleteConfig(key);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage(), "NOT_FOUND"));
        }
    }
    
    /**
     * POST /api/v1/admin/config/bulk
     * Updates multiple configs at once.
     * Accepts a nested JSON object that will be flattened to dot-notation keys.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/bulk")
    public ResponseEntity<?> bulkUpdateConfigs(
            @RequestHeader(value = "Authorization") String authorizationHeader,
            @RequestBody java.util.Map<String, Object> configs) {
        try {
            User currentUser = getCurrentUser(authorizationHeader);
            java.util.Map<String, String> flattenedConfigs = flattenMap(configs, "");
            configService.bulkUpdateConfigs(flattenedConfigs, currentUser);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage(), "VALIDATION_ERROR"));
        }
    }
    
    /**
     * Recursively flattens a nested map into dot-notation keys.
     */
    @SuppressWarnings("unchecked")
    private java.util.Map<String, String> flattenMap(java.util.Map<String, Object> map, String prefix) {
        java.util.Map<String, String> result = new java.util.HashMap<>();
        for (java.util.Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();
            if (value instanceof java.util.Map) {
                result.putAll(flattenMap((java.util.Map<String, Object>) value, key));
            } else if (value != null) {
                result.put(key, value.toString());
            } else {
                result.put(key, null);
            }
        }
        return result;
    }
    
    // Helper method
    private User getCurrentUser(String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        String email = jwtService.extractUserName(token);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
