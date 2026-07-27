package icpmapp.controller;

import icpmapp.dto.requests.*;
import icpmapp.dto.responses.*;
import icpmapp.entities.Page;
import icpmapp.entities.PageMessage;
import icpmapp.entities.User;
import icpmapp.repository.UserRepository;
import icpmapp.services.JWTService;
import icpmapp.services.PageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/pages")
@RequiredArgsConstructor
public class AdminPageController {
    
    private final PageService pageService;
    private final JWTService jwtService;
    private final UserRepository userRepository;
    
    /**
     * GET /api/v1/admin/pages
     * Returns all pages (including unpublished) with full details.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping
    public ResponseEntity<AdminPageListResponse> getAllPages(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean published,
            @RequestParam(required = false, defaultValue = "sortOrder") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir) {
        return ResponseEntity.ok(pageService.getAllPages(search, published, sortBy, sortDir));
    }
    
    /**
     * GET /api/v1/admin/pages/{id}
     * Returns single page with all fields for editing.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getPageById(@PathVariable UUID id) {
        return pageService.getAdminPageById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Page not found", "NOT_FOUND")));
    }
    
    /**
     * POST /api/v1/admin/pages
     * Creates a new page.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping
    public ResponseEntity<?> createPage(
            @RequestHeader(value = "Authorization") String authorizationHeader,
            @RequestBody PageRequest request) {
        try {
            User currentUser = getCurrentUser(authorizationHeader);
            Page page = pageService.create(request, currentUser);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(pageService.getAdminPageById(page.getId()).orElse(null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage(), "VALIDATION_ERROR"));
        }
    }
    
    /**
     * PUT /api/v1/admin/pages/{id}
     * Updates an existing page.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePage(
            @PathVariable UUID id,
            @RequestHeader(value = "Authorization") String authorizationHeader,
            @RequestBody PageRequest request) {
        try {
            User currentUser = getCurrentUser(authorizationHeader);
            Page page = pageService.update(id, request, currentUser);
            return ResponseEntity.ok(pageService.getAdminPageById(page.getId()).orElse(null));
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
     * DELETE /api/v1/admin/pages/{id}
     * Deletes a page.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePage(@PathVariable UUID id) {
        try {
            pageService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage(), "NOT_FOUND"));
        }
    }
    
    /**
     * PUT /api/v1/admin/pages/reorder
     * Bulk update sort order.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/reorder")
    public ResponseEntity<?> reorderPages(@RequestBody PageReorderRequest request) {
        try {
            pageService.reorderPages(request);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage(), "VALIDATION_ERROR"));
        }
    }
    
    /**
     * POST /api/v1/admin/pages/{id}/duplicate
     * Duplicates a page with a new slug.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/{id}/duplicate")
    public ResponseEntity<?> duplicatePage(
            @PathVariable UUID id,
            @RequestHeader(value = "Authorization") String authorizationHeader,
            @RequestBody PageDuplicateRequest request) {
        try {
            User currentUser = getCurrentUser(authorizationHeader);
            Page page = pageService.duplicate(id, request, currentUser);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(pageService.getAdminPageById(page.getId()).orElse(null));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse(e.getMessage(), "NOT_FOUND"));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage(), "VALIDATION_ERROR"));
        }
    }
    
    // ==================== Page Messages ====================
    
    /**
     * GET /api/v1/admin/pages/{id}/messages
     * Returns all messages for a page.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/{id}/messages")
    public ResponseEntity<List<PageDetailResponse.PageMessageResponse>> getPageMessages(@PathVariable UUID id) {
        return ResponseEntity.ok(pageService.getPageMessages(id));
    }
    
    /**
     * POST /api/v1/admin/pages/{id}/messages
     * Creates a new message for a page.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/{id}/messages")
    public ResponseEntity<?> createPageMessage(
            @PathVariable UUID id,
            @RequestBody PageMessageRequest request) {
        try {
            PageMessage message = pageService.createMessage(id, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(PageDetailResponse.PageMessageResponse.builder()
                    .id(message.getId())
                    .content(message.getContent())
                    .sortOrder(message.getSortOrder())
                    .createdAt(message.getCreatedAt())
                    .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage(), "NOT_FOUND"));
        }
    }
    
    /**
     * PUT /api/v1/admin/pages/{id}/messages/{messageId}
     * Updates a page message.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{id}/messages/{messageId}")
    public ResponseEntity<?> updatePageMessage(
            @PathVariable UUID id,
            @PathVariable UUID messageId,
            @RequestBody PageMessageRequest request) {
        try {
            PageMessage message = pageService.updateMessage(id, messageId, request);
            return ResponseEntity.ok(PageDetailResponse.PageMessageResponse.builder()
                    .id(message.getId())
                    .content(message.getContent())
                    .sortOrder(message.getSortOrder())
                    .createdAt(message.getCreatedAt())
                    .build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage(), "NOT_FOUND"));
        }
    }
    
    /**
     * DELETE /api/v1/admin/pages/{id}/messages/{messageId}
     * Deletes a page message.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}/messages/{messageId}")
    public ResponseEntity<?> deletePageMessage(
            @PathVariable UUID id,
            @PathVariable UUID messageId) {
        try {
            pageService.deleteMessage(id, messageId);
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
