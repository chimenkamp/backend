package icpmapp.services.impl;

import icpmapp.dto.requests.LocalizedContentBulkRequest;
import icpmapp.dto.requests.LocalizedContentRequest;
import icpmapp.dto.responses.LanguageSummaryResponse;
import icpmapp.entities.LocalizedContent;
import icpmapp.entities.User;
import icpmapp.repository.LocalizedContentRepository;
import icpmapp.services.LocaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocaleServiceImpl implements LocaleService {
    
    private final LocalizedContentRepository localizedContentRepository;
    
    @Override
    public Map<String, String> getLocaleContent(String languageCode) {
        return localizedContentRepository.findByLanguageCode(languageCode).stream()
                .collect(Collectors.toMap(
                        LocalizedContent::getContentKey,
                        LocalizedContent::getContentValue,
                        (existing, replacement) -> existing // Keep first in case of duplicates
                ));
    }
    
    @Override
    public List<LanguageSummaryResponse> getAllLanguages() {
        List<String> languageCodes = localizedContentRepository.findDistinctLanguageCodes();
        return languageCodes.stream()
                .map(code -> LanguageSummaryResponse.builder()
                        .languageCode(code)
                        .contentCount(localizedContentRepository.countByLanguageCode(code))
                        .build())
                .collect(Collectors.toList());
    }
    
    @Override
    public Map<String, String> getLanguageContent(String languageCode) {
        return getLocaleContent(languageCode);
    }
    
    @Override
    @Transactional
    public LocalizedContent updateContent(String languageCode, String contentKey, LocalizedContentRequest request, User updatedBy) {
        LocalizedContent content = localizedContentRepository.findByContentKeyAndLanguageCode(contentKey, languageCode)
                .orElseGet(() -> {
                    LocalizedContent newContent = new LocalizedContent();
                    newContent.setContentKey(contentKey);
                    newContent.setLanguageCode(languageCode);
                    return newContent;
                });
        
        content.setContentValue(request.getValue());
        if (request.getType() != null) {
            content.setContentType(request.getType());
        }
        content.setUpdatedBy(updatedBy);
        
        return localizedContentRepository.save(content);
    }
    
    @Override
    @Transactional
    public void bulkUpdateContent(String languageCode, LocalizedContentBulkRequest request, User updatedBy) {
        for (Map.Entry<String, String> entry : request.getContent().entrySet()) {
            LocalizedContent content = localizedContentRepository.findByContentKeyAndLanguageCode(entry.getKey(), languageCode)
                    .orElseGet(() -> {
                        LocalizedContent newContent = new LocalizedContent();
                        newContent.setContentKey(entry.getKey());
                        newContent.setLanguageCode(languageCode);
                        return newContent;
                    });
            
            content.setContentValue(entry.getValue());
            content.setUpdatedBy(updatedBy);
            localizedContentRepository.save(content);
        }
    }
    
    @Override
    @Transactional
    public void deleteContent(String languageCode, String contentKey) {
        localizedContentRepository.deleteByContentKeyAndLanguageCode(contentKey, languageCode);
    }
}
