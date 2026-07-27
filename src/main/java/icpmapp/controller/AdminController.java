package icpmapp.controller;

import icpmapp.dto.requests.*;
import icpmapp.dto.responses.*;
import icpmapp.entities.ContentCategory;
import icpmapp.entities.ContentType;
import icpmapp.services.AdminService;
import icpmapp.services.JWTService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final JWTService jwtService;

    // ==================== Dashboard ====================

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    // ==================== Content Management ====================

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/content")
    public ResponseEntity<List<ContentResponse>> getAllContent(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String search) {
        
        ContentCategory contentCategory = null;
        ContentType contentType = null;
        
        if (category != null) {
            try {
                contentCategory = ContentCategory.valueOf(category.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid category, ignore
            }
        }
        if (type != null) {
            try {
                contentType = ContentType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid type, ignore
            }
        }
        
        return ResponseEntity.ok(adminService.getAllContent(contentCategory, contentType, search));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/content/{id}")
    public ResponseEntity<?> getContentById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(adminService.getContentById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Content not found", "CONTENT_NOT_FOUND"));
        }
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/content")
    public ResponseEntity<ContentResponse> createContent(
            @RequestHeader(value = "Authorization") String authorizationHeader,
            @RequestBody ContentRequest request) {
        String token = authorizationHeader.substring(7);
        String adminUserId = jwtService.extractUserName(token);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminService.createContent(request, adminUserId));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/content/{id}")
    public ResponseEntity<?> updateContent(
            @PathVariable String id,
            @RequestHeader(value = "Authorization") String authorizationHeader,
            @RequestBody UpdateContentRequest request) {
        try {
            String token = authorizationHeader.substring(7);
            String adminUserId = jwtService.extractUserName(token);
            return ResponseEntity.ok(adminService.updateContent(id, request, adminUserId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Content not found", "CONTENT_NOT_FOUND"));
        }
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/content/{id}")
    public ResponseEntity<?> deleteContent(@PathVariable String id) {
        try {
            adminService.deleteContent(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Content not found", "CONTENT_NOT_FOUND"));
        }
    }

    // ==================== User Management ====================

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<UserListResponse> getUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String filter) {
        return ResponseEntity.ok(adminService.getUsers(page, limit, search, filter));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(adminService.getUserById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("User not found", "USER_NOT_FOUND"));
        }
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(adminService.createUser(request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage(), "USER_CREATE_ERROR"));
        }
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Integer id,
            @RequestBody UpdateUserRequest request) {
        try {
            return ResponseEntity.ok(adminService.updateUser(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("User not found", "USER_NOT_FOUND"));
        }
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
        try {
            adminService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("User not found", "USER_NOT_FOUND"));
        }
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/users/{id}/reset-password")
    public ResponseEntity<?> resetUserPassword(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(adminService.resetUserPassword(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("User not found", "USER_NOT_FOUND"));
        }
    }

    // ==================== Messaging System ====================

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/messages")
    public ResponseEntity<AdminMessageListResponse> getMessages(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(adminService.getMessages(page, limit));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/messages/{id}")
    public ResponseEntity<?> getMessageById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(adminService.getMessageById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Message not found", "MESSAGE_NOT_FOUND"));
        }
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/messages")
    public ResponseEntity<?> sendMessage(
            @RequestHeader(value = "Authorization") String authorizationHeader,
            @RequestBody AdminMessageRequest request) {
        try {
            String token = authorizationHeader.substring(7);
            String adminUserId = jwtService.extractUserName(token);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(adminService.sendMessage(request, adminUserId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage(), "NO_RECIPIENTS"));
        }
    }

    // ==================== Schedule Designer - Sessions ====================

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/schedule/sessions")
    public ResponseEntity<List<SessionResponse>> getSessions(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String track,
            @RequestParam(required = false) Boolean published) {
        
        LocalDateTime dateTime = null;
        if (date != null) {
            try {
                dateTime = LocalDate.parse(date).atStartOfDay();
            } catch (Exception e) {
                // Invalid date format, ignore
            }
        }
        
        return ResponseEntity.ok(adminService.getSessions(dateTime, track, published));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/schedule/sessions/{id}")
    public ResponseEntity<?> getSessionById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(adminService.getSessionById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Session not found", "SESSION_NOT_FOUND"));
        }
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/schedule/sessions")
    public ResponseEntity<SessionResponse> createSession(@RequestBody SessionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminService.createSession(request));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/schedule/sessions/{id}")
    public ResponseEntity<?> updateSession(
            @PathVariable Long id,
            @RequestBody SessionRequest request) {
        try {
            return ResponseEntity.ok(adminService.updateSession(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Session not found", "SESSION_NOT_FOUND"));
        }
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/schedule/sessions/{id}")
    public ResponseEntity<?> deleteSession(@PathVariable Long id) {
        try {
            adminService.deleteSession(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Session not found", "SESSION_NOT_FOUND"));
        }
    }

    // ==================== Schedule Designer - Tracks ====================

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/schedule/tracks")
    public ResponseEntity<List<TrackResponse>> getTracks() {
        return ResponseEntity.ok(adminService.getTracks());
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/schedule/tracks")
    public ResponseEntity<List<TrackResponse>> updateTracks(@RequestBody List<TrackRequest> tracks) {
        return ResponseEntity.ok(adminService.updateTracks(tracks));
    }

    // ==================== Schedule Designer - Locations ====================

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/schedule/locations")
    public ResponseEntity<List<LocationResponse>> getLocations() {
        return ResponseEntity.ok(adminService.getLocations());
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/schedule/locations")
    public ResponseEntity<List<LocationResponse>> updateLocations(@RequestBody List<LocationRequest> locations) {
        return ResponseEntity.ok(adminService.updateLocations(locations));
    }

    // ==================== Legacy endpoint ====================

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping
    public ResponseEntity<String> sayHello() {
        return ResponseEntity.ok("Hi ADMIN");
    }
}
