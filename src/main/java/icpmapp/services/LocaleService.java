package icpmapp.services;

import icpmapp.dto.requests.LocalizedContentBulkRequest;
import icpmapp.dto.requests.LocalizedContentRequest;
import icpmapp.dto.responses.LanguageSummaryResponse;
import icpmapp.entities.LocalizedContent;
import icpmapp.entities.User;

import java.util.List;
import java.util.Map;

public interface LocaleService {
    
    // Public endpoints
    Map<String, String> getLocaleContent(String languageCode);
    
    // Admin endpoints
    List<LanguageSummaryResponse> getAllLanguages();
    Map<String, String> getLanguageContent(String languageCode);
    LocalizedContent updateContent(String languageCode, String contentKey, LocalizedContentRequest request, User updatedBy);
    void bulkUpdateContent(String languageCode, LocalizedContentBulkRequest request, User updatedBy);
    void deleteContent(String languageCode, String contentKey);
}
