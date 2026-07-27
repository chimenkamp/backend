package icpmapp.services;

import icpmapp.dto.requests.SiteConfigBulkRequest;
import icpmapp.dto.requests.SiteConfigCreateRequest;
import icpmapp.dto.requests.SiteConfigRequest;
import icpmapp.dto.responses.ConfigVersionResponse;
import icpmapp.dto.responses.PublicSiteConfigResponse;
import icpmapp.dto.responses.SiteConfigDetailResponse;
import icpmapp.entities.SiteConfig;
import icpmapp.entities.User;

import java.util.List;
import java.util.Optional;

public interface ConfigService {
    
    // Public endpoints
    PublicSiteConfigResponse getPublicSiteConfig();
    ConfigVersionResponse getConfigVersion();
    
    // Admin endpoints
    List<SiteConfigDetailResponse> getAllConfigs();
    Optional<SiteConfigDetailResponse> getConfigByKey(String key);
    SiteConfig updateConfig(String key, SiteConfigRequest request, User updatedBy);
    SiteConfig createConfig(SiteConfigCreateRequest request, User updatedBy);
    void deleteConfig(String key);
    void bulkUpdateConfigs(java.util.Map<String, String> configs, User updatedBy);
    
    // Cache management
    void invalidateCache();
    String generateVersionHash();
}
