package icpmapp.services;

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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PageService {
    
    // Public endpoints
    List<PageListResponse> getPublishedPages();
    List<PageListResponse> getPublicPages();
    Optional<PageDetailResponse> getPageById(UUID id);
    Optional<PageDetailResponse> getPageBySlug(String slug);
    
    // Admin endpoints
    AdminPageListResponse getAllPages(String search, Boolean published, String sortBy, String sortDir);
    Optional<PageDetailResponse> getAdminPageById(UUID id);
    Page create(PageRequest pageRequest, User createdBy);
    Page update(UUID id, PageRequest pageRequest, User updatedBy);
    void delete(UUID id);
    void reorderPages(PageReorderRequest reorderRequest);
    Page duplicate(UUID id, PageDuplicateRequest duplicateRequest, User createdBy);
    
    // Page messages
    List<PageDetailResponse.PageMessageResponse> getPageMessages(UUID pageId);
    PageMessage createMessage(UUID pageId, PageMessageRequest messageRequest);
    PageMessage updateMessage(UUID pageId, UUID messageId, PageMessageRequest messageRequest);
    void deleteMessage(UUID pageId, UUID messageId);
    
    // Legacy methods for backward compatibility
    Page create(PageRequest pageRequest);
    void delete(Integer id);
    Optional<Page> findById(Integer id);
    Page update(PageRequest pageRequest, Integer id);
}


