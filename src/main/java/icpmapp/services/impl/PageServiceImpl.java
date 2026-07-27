package icpmapp.services.impl;

import icpmapp.dto.requests.PageDuplicateRequest;
import icpmapp.dto.requests.PageMessageRequest;
import icpmapp.dto.requests.PageReorderRequest;
import icpmapp.dto.requests.PageRequest;
import icpmapp.dto.responses.AdminPageListResponse;
import icpmapp.dto.responses.PageDetailResponse;
import icpmapp.dto.responses.PageListResponse;
import icpmapp.entities.Page;
import icpmapp.entities.PageMessage;
import icpmapp.entities.User;
import icpmapp.repository.PageMessageRepository;
import icpmapp.repository.PageRepository;
import icpmapp.services.PageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PageServiceImpl implements PageService {
    private final PageRepository pageRepository;
    private final PageMessageRepository pageMessageRepository;

    // Public endpoints
    @Override
    public List<PageListResponse> getPublishedPages() {
        return pageRepository.findByIsPublishedTrueOrderBySortOrderAsc().stream()
                .map(this::toListResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PageListResponse> getPublicPages() {
        return pageRepository.findByIsPublishedTrueAndIsPublicTrueOrderBySortOrderAsc().stream()
                .map(this::toListResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PageDetailResponse> getPageById(UUID id) {
        return pageRepository.findById(id)
                .filter(Page::getIsPublished)
                .map(this::toDetailResponse);
    }

    @Override
    public Optional<PageDetailResponse> getPageBySlug(String slug) {
        return pageRepository.findBySlug(slug)
                .filter(Page::getIsPublished)
                .map(this::toDetailResponse);
    }

    // Admin endpoints
    @Override
    public AdminPageListResponse getAllPages(String search, Boolean published, String sortBy, String sortDir) {
        // Format search term for LIKE query with wildcards and lowercase
        String searchPattern = (search != null && !search.isEmpty()) 
                ? "%" + search.toLowerCase() + "%" 
                : null;
        List<Page> pages = pageRepository.findByFilters(searchPattern, published);
        
        // Apply sorting
        if (sortBy != null && !sortBy.isEmpty()) {
            Comparator<Page> comparator = switch (sortBy.toLowerCase()) {
                case "title" -> Comparator.comparing(Page::getTitle, String.CASE_INSENSITIVE_ORDER);
                case "createdat" -> Comparator.comparing(Page::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()));
                case "updatedat" -> Comparator.comparing(Page::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder()));
                default -> Comparator.comparing(Page::getSortOrder);
            };
            
            if ("desc".equalsIgnoreCase(sortDir)) {
                comparator = comparator.reversed();
            }
            pages = pages.stream().sorted(comparator).collect(Collectors.toList());
        }
        
        List<PageDetailResponse> responseList = pages.stream()
                .map(this::toDetailResponse)
                .collect(Collectors.toList());
        
        return AdminPageListResponse.builder()
                .pages(responseList)
                .total(responseList.size())
                .build();
    }

    @Override
    public Optional<PageDetailResponse> getAdminPageById(UUID id) {
        return pageRepository.findById(id).map(this::toDetailResponse);
    }

    @Override
    @Transactional
    public Page create(PageRequest pageRequest, User createdBy) {
        validateSlug(pageRequest.getSlug(), null);
        
        Page page = new Page();
        mapRequestToPage(pageRequest, page);
        page.setCreatedBy(createdBy);
        page.setUpdatedBy(createdBy);
        
        return pageRepository.save(page);
    }

    @Override
    @Transactional
    public Page update(UUID id, PageRequest pageRequest, User updatedBy) {
        Page page = pageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Page not found"));
        
        // Validate slug if changed
        if (pageRequest.getSlug() != null && !pageRequest.getSlug().equals(page.getSlug())) {
            validateSlug(pageRequest.getSlug(), id);
        }
        
        mapRequestToPage(pageRequest, page);
        page.setUpdatedBy(updatedBy);
        
        return pageRepository.save(page);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!pageRepository.existsById(id)) {
            throw new RuntimeException("Page not found");
        }
        pageRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void reorderPages(PageReorderRequest reorderRequest) {
        for (PageReorderRequest.PageOrderItem item : reorderRequest.getPages()) {
            pageRepository.findById(item.getId()).ifPresent(page -> {
                page.setSortOrder(item.getSortOrder());
                pageRepository.save(page);
            });
        }
    }

    @Override
    @Transactional
    public Page duplicate(UUID id, PageDuplicateRequest duplicateRequest, User createdBy) {
        Page original = pageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Page not found"));
        
        validateSlug(duplicateRequest.getNewSlug(), null);
        
        Page copy = new Page();
        copy.setTitle(duplicateRequest.getNewTitle() != null ? duplicateRequest.getNewTitle() : original.getTitle() + " (Copy)");
        copy.setSlug(duplicateRequest.getNewSlug());
        copy.setContent(original.getContent());
        copy.setLayoutId(original.getLayoutId());
        copy.setSortOrder(original.getSortOrder() + 1);
        copy.setIsPublished(false); // Duplicates are unpublished by default
        copy.setIsPublic(original.getIsPublic());
        copy.setLabel(original.getLabel());
        copy.setLabelColor(original.getLabelColor());
        copy.setIcon(original.getIcon());
        copy.setCreatedBy(createdBy);
        copy.setUpdatedBy(createdBy);
        
        return pageRepository.save(copy);
    }

    // Page messages
    @Override
    public List<PageDetailResponse.PageMessageResponse> getPageMessages(UUID pageId) {
        return pageMessageRepository.findByPageIdOrderBySortOrderAsc(pageId).stream()
                .map(this::toMessageResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PageMessage createMessage(UUID pageId, PageMessageRequest messageRequest) {
        Page page = pageRepository.findById(pageId)
                .orElseThrow(() -> new RuntimeException("Page not found"));
        
        PageMessage message = new PageMessage();
        message.setPage(page);
        message.setContent(messageRequest.getContent());
        message.setSortOrder(messageRequest.getSortOrder() != null ? messageRequest.getSortOrder() : 0);
        
        return pageMessageRepository.save(message);
    }

    @Override
    @Transactional
    public PageMessage updateMessage(UUID pageId, UUID messageId, PageMessageRequest messageRequest) {
        PageMessage message = pageMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        
        if (!message.getPage().getId().equals(pageId)) {
            throw new RuntimeException("Message does not belong to this page");
        }
        
        if (messageRequest.getContent() != null) {
            message.setContent(messageRequest.getContent());
        }
        if (messageRequest.getSortOrder() != null) {
            message.setSortOrder(messageRequest.getSortOrder());
        }
        
        return pageMessageRepository.save(message);
    }

    @Override
    @Transactional
    public void deleteMessage(UUID pageId, UUID messageId) {
        PageMessage message = pageMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        
        if (!message.getPage().getId().equals(pageId)) {
            throw new RuntimeException("Message does not belong to this page");
        }
        
        pageMessageRepository.delete(message);
    }

    // Legacy methods for backward compatibility
    @Override
    public Page create(PageRequest pageRequest) {
        Page page = new Page();
        page.setTitle(pageRequest.getTitle());
        page.setContent(pageRequest.getContent());
        page.setLayoutId(pageRequest.getLayoutId() != null ? pageRequest.getLayoutId() : 1);
        page.setSlug(generateSlug(pageRequest.getTitle()));
        return pageRepository.save(page);
    }

    @Override
    public void delete(Integer id) {
        // Legacy method - convert to UUID search
        pageRepository.findAll().stream()
                .filter(p -> p.getId().toString().hashCode() == id)
                .findFirst()
                .ifPresent(pageRepository::delete);
    }

    @Override
    public Optional<Page> findById(Integer id) {
        // Legacy method - not applicable with UUID
        return Optional.empty();
    }

    @Override
    public Page update(PageRequest updateRequest, Integer id) {
        // Legacy method - not applicable with UUID
        throw new RuntimeException("Use update(UUID id, PageRequest, User) instead");
    }

    // Helper methods
    private void validateSlug(String slug, UUID excludeId) {
        if (slug == null || slug.isEmpty()) {
            throw new RuntimeException("Slug is required");
        }
        
        if (!slug.matches("^[a-z0-9]+(-[a-z0-9]+)*$")) {
            throw new RuntimeException("Slug must be lowercase, alphanumeric with hyphens only");
        }
        
        Optional<Page> existing = pageRepository.findBySlug(slug);
        if (existing.isPresent() && (excludeId == null || !existing.get().getId().equals(excludeId))) {
            throw new RuntimeException("Slug already exists");
        }
    }

    private String generateSlug(String title) {
        if (title == null) return "page-" + System.currentTimeMillis();
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    private void mapRequestToPage(PageRequest request, Page page) {
        if (request.getTitle() != null) page.setTitle(request.getTitle());
        if (request.getSlug() != null) page.setSlug(request.getSlug());
        if (request.getContent() != null) page.setContent(request.getContent());
        if (request.getLayoutId() != null) page.setLayoutId(request.getLayoutId());
        if (request.getSortOrder() != null) page.setSortOrder(request.getSortOrder());
        if (request.getIsPublished() != null) page.setIsPublished(request.getIsPublished());
        if (request.getIsPublic() != null) page.setIsPublic(request.getIsPublic());
        if (request.getLabel() != null) page.setLabel(request.getLabel().isEmpty() ? null : request.getLabel());
        if (request.getLabelColor() != null) page.setLabelColor(request.getLabelColor());
        if (request.getIcon() != null) page.setIcon(request.getIcon().isEmpty() ? null : request.getIcon());
    }

    private PageListResponse toListResponse(Page page) {
        return PageListResponse.builder()
                .id(page.getId())
                .title(page.getTitle())
                .slug(page.getSlug())
                .layoutId(page.getLayoutId())
                .sortOrder(page.getSortOrder())
                .label(page.getLabel())
                .labelColor(page.getLabelColor())
                .icon(page.getIcon())
                .build();
    }

    private PageDetailResponse toDetailResponse(Page page) {
        List<PageDetailResponse.PageMessageResponse> messages = null;
        if (page.getLayoutId() != null && page.getLayoutId() == 2) {
            messages = page.getMessages().stream()
                    .map(this::toMessageResponse)
                    .collect(Collectors.toList());
        }
        
        return PageDetailResponse.builder()
                .id(page.getId())
                .title(page.getTitle())
                .slug(page.getSlug())
                .content(page.getContent())
                .layoutId(page.getLayoutId())
                .sortOrder(page.getSortOrder())
                .isPublished(page.getIsPublished())
                .isPublic(page.getIsPublic())
                .label(page.getLabel())
                .labelColor(page.getLabelColor())
                .icon(page.getIcon())
                .createdAt(page.getCreatedAt())
                .updatedAt(page.getUpdatedAt())
                .createdBy(toUserSummary(page.getCreatedBy()))
                .updatedBy(toUserSummary(page.getUpdatedBy()))
                .messages(messages)
                .build();
    }

    private PageDetailResponse.UserSummary toUserSummary(User user) {
        if (user == null) return null;
        return PageDetailResponse.UserSummary.builder()
                .id(user.getId())
                .name(user.getFirstname() + " " + user.getLastname())
                .build();
    }

    private PageDetailResponse.PageMessageResponse toMessageResponse(PageMessage message) {
        return PageDetailResponse.PageMessageResponse.builder()
                .id(message.getId())
                .content(message.getContent())
                .sortOrder(message.getSortOrder())
                .createdAt(message.getCreatedAt())
                .build();
    }
}

