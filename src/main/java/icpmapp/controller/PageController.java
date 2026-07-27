package icpmapp.controller;

import icpmapp.dto.responses.PageDetailResponse;
import icpmapp.dto.responses.PageListResponse;
import icpmapp.services.PageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pages")
@RequiredArgsConstructor
public class PageController {
    private final PageService pageService;

    /**
     * GET /api/v1/pages
     * Returns list of published pages for navigation.
     * Authenticated users see all published pages, unauthenticated see only public pages.
     */
    @GetMapping
    public ResponseEntity<List<PageListResponse>> getPages(
            @RequestParam(required = false, defaultValue = "true") Boolean published) {
        // For public endpoint, always return published pages
        List<PageListResponse> pages = pageService.getPublishedPages();
        return ResponseEntity.ok(pages);
    }

    /**
     * GET /api/v1/pages/public
     * Returns only public pages (accessible without login)
     */
    @GetMapping("/public")
    public ResponseEntity<List<PageListResponse>> getPublicPages() {
        return ResponseEntity.ok(pageService.getPublicPages());
    }

    /**
     * GET /api/v1/pages/{id}
     * Returns full page content by ID.
     */
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<PageDetailResponse> getPageById(@PathVariable UUID id) {
        return pageService.getPageById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/v1/pages/slug/{slug}
     * Returns page by slug (alternative to ID lookup).
     */
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    @GetMapping("/slug/{slug}")
    public ResponseEntity<PageDetailResponse> getPageBySlug(@PathVariable String slug) {
        return pageService.getPageBySlug(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

