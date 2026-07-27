package icpmapp.controller;

import icpmapp.dto.requests.NavigationConfigRequest;
import icpmapp.dto.requests.NavigationReorderRequest;
import icpmapp.dto.responses.ErrorResponse;
import icpmapp.dto.responses.NavigationConfigDetailResponse;
import icpmapp.services.NavigationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/navigation")
@RequiredArgsConstructor
public class AdminNavigationController {
    
    private final NavigationService navigationService;
    
    /**
     * GET /api/v1/admin/navigation
     * Returns all navigation items.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping
    public ResponseEntity<List<NavigationConfigDetailResponse>> getAllNavigation() {
        return ResponseEntity.ok(navigationService.getAllNavigation());
    }
    
    /**
     * PUT /api/v1/admin/navigation/{tabKey}
     * Updates a navigation item.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{tabKey}")
    public ResponseEntity<?> updateNavigation(
            @PathVariable String tabKey,
            @RequestBody NavigationConfigRequest request) {
        try {
            navigationService.updateNavigation(tabKey, request);
            return ResponseEntity.ok().build();
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
     * PUT /api/v1/admin/navigation/reorder
     * Reorders navigation tabs.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/reorder")
    public ResponseEntity<?> reorderNavigation(@RequestBody NavigationReorderRequest request) {
        try {
            navigationService.reorderNavigation(request);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage(), "VALIDATION_ERROR"));
        }
    }
}
