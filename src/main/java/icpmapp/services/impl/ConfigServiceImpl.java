package icpmapp.services.impl;

import icpmapp.config.ConfigCatalog;
import icpmapp.dto.requests.SiteConfigBulkRequest;
import icpmapp.dto.requests.SiteConfigCreateRequest;
import icpmapp.dto.requests.SiteConfigRequest;
import icpmapp.dto.responses.*;
import icpmapp.entities.NavigationConfig;
import icpmapp.entities.SiteConfig;
import icpmapp.entities.User;
import icpmapp.repository.NavigationConfigRepository;
import icpmapp.repository.SiteConfigRepository;
import icpmapp.services.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {
    
    private final SiteConfigRepository siteConfigRepository;
    private final NavigationConfigRepository navigationConfigRepository;
    
    // Simple in-memory cache
    private volatile PublicSiteConfigResponse cachedConfig = null;
    private volatile LocalDateTime cacheExpiry = null;
    private static final long CACHE_DURATION_MINUTES = 5;
    
    @Override
    public PublicSiteConfigResponse getPublicSiteConfig() {
        // Check cache
        if (cachedConfig != null && cacheExpiry != null && LocalDateTime.now().isBefore(cacheExpiry)) {
            return cachedConfig;
        }
        
        // Build config from database
        List<SiteConfig> publicConfigs = siteConfigRepository.findByIsPublicTrue();
        Map<String, String> configMap = publicConfigs.stream()
                .collect(Collectors.toMap(SiteConfig::getConfigKey, 
                        c -> c.getConfigValue() != null ? c.getConfigValue() : ""));
        
        // Build conference config
        PublicSiteConfigResponse.ConferenceConfig conferenceConfig = PublicSiteConfigResponse.ConferenceConfig.builder()
                .name(configMap.getOrDefault("conference.name", ""))
                .tagline(configMap.getOrDefault("conference.tagline", ""))
                .description(configMap.getOrDefault("conference.description", ""))
                .dates(PublicSiteConfigResponse.ConferenceConfig.DateRange.builder()
                        .start(configMap.getOrDefault("conference.dates.start", ""))
                        .end(configMap.getOrDefault("conference.dates.end", ""))
                        .build())
                .location(configMap.getOrDefault("conference.location", ""))
                .timezone(configMap.getOrDefault("conference.timezone", ""))
                .build();
        
        // Build branding config
        PublicSiteConfigResponse.BrandingConfig brandingConfig = PublicSiteConfigResponse.BrandingConfig.builder()
                .logoLight(configMap.getOrDefault("branding.logoLight", ""))
                .logoDark(configMap.getOrDefault("branding.logoDark", ""))
                .homeImage(configMap.getOrDefault("branding.homeImage", ""))
                .primaryColor(configMap.getOrDefault("branding.primaryColor", "#3880ff"))
                .secondaryColor(configMap.getOrDefault("branding.secondaryColor", "#3dc2ff"))
                .accentColor(configMap.getOrDefault("branding.accentColor", "#5260ff"))
                .favicon(configMap.getOrDefault("branding.favicon", ""))
                .build();
        
        // Build features config
        PublicSiteConfigResponse.FeaturesConfig featuresConfig = PublicSiteConfigResponse.FeaturesConfig.builder()
                .galleryEnabled(parseBoolean(configMap.getOrDefault("features.galleryEnabled", "false")))
                .attendeesVisible(parseBoolean(configMap.getOrDefault("features.attendeesVisible", "false")))
                .messagesEnabled(parseBoolean(configMap.getOrDefault("features.messagesEnabled", "false")))
                .registrationOpen(parseBoolean(configMap.getOrDefault("features.registrationOpen", "false")))
                .agendaEnabled(parseBoolean(configMap.getOrDefault("features.agendaEnabled", "false")))
                .myGalleryEnabled(parseBoolean(configMap.getOrDefault("features.myGalleryEnabled", "false")))
                .build();
        
        // Build social config
        PublicSiteConfigResponse.SocialConfig socialConfig = PublicSiteConfigResponse.SocialConfig.builder()
                .twitterUrl(emptyToNull(configMap.getOrDefault("social.twitter", "")))
                .linkedinUrl(emptyToNull(configMap.getOrDefault("social.linkedin", "")))
                .websiteUrl(emptyToNull(configMap.getOrDefault("social.website", "")))
                .registrationUrl(emptyToNull(configMap.getOrDefault("social.registration", "")))
                .build();

        PublicSiteConfigResponse.ContactConfig contactConfig = PublicSiteConfigResponse.ContactConfig.builder()
                .email(emptyToNull(configMap.getOrDefault("contact.email", "")))
                .name(emptyToNull(configMap.getOrDefault("contact.name", "")))
                .organization(emptyToNull(configMap.getOrDefault("contact.organization", "")))
                .phone(emptyToNull(configMap.getOrDefault("contact.phone", "")))
                .build();
        
        // Build navigation
        List<NavigationItemResponse> navigation = navigationConfigRepository.findByIsEnabledTrueOrderBySortOrderAsc().stream()
                .map(this::toNavigationItemResponse)
                .collect(Collectors.toList());
        
        PublicSiteConfigResponse response = PublicSiteConfigResponse.builder()
                .conference(conferenceConfig)
                .branding(brandingConfig)
                .features(featuresConfig)
                .navigation(navigation)
                .social(socialConfig)
                .contact(contactConfig)
                .version(generateVersionHash())
                .build();
        
        // Update cache
        cachedConfig = response;
        cacheExpiry = LocalDateTime.now().plusMinutes(CACHE_DURATION_MINUTES);
        
        return response;
    }
    
    @Override
    public ConfigVersionResponse getConfigVersion() {
        Optional<LocalDateTime> lastUpdate = siteConfigRepository.findAll().stream()
                .map(SiteConfig::getUpdatedAt)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo);
        
        return ConfigVersionResponse.builder()
                .version(generateVersionHash())
                .lastUpdated(lastUpdate.orElse(LocalDateTime.now()))
                .build();
    }
    
    @Override
    public List<SiteConfigDetailResponse> getAllConfigs() {
        return siteConfigRepository.findAll().stream()
                .map(this::toDetailResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<SiteConfigDetailResponse> getConfigByKey(String key) {
        return siteConfigRepository.findByConfigKey(key).map(this::toDetailResponse);
    }
    
    @Override
    @Transactional
    public SiteConfig updateConfig(String key, SiteConfigRequest request, User updatedBy) {
        SiteConfig config = siteConfigRepository.findByConfigKey(key)
                .orElseThrow(() -> new RuntimeException("Config not found: " + key));
        
        config.setConfigValue(request.getValue());
        if (request.getType() != null) {
            config.setConfigType(request.getType());
        }
        config.setUpdatedBy(updatedBy);
        
        invalidateCache();
        return siteConfigRepository.save(config);
    }
    
    @Override
    @Transactional
    public SiteConfig createConfig(SiteConfigCreateRequest request, User updatedBy) {
        if (siteConfigRepository.existsByConfigKey(request.getKey())) {
            throw new RuntimeException("Config key already exists: " + request.getKey());
        }
        
        SiteConfig config = new SiteConfig();
        config.setConfigKey(request.getKey());
        config.setConfigValue(request.getValue());
        config.setConfigType(request.getType() != null ? request.getType() : "text");
        config.setCategory(request.getCategory() != null ? request.getCategory() : "other");
        config.setDescription(request.getDescription());
        config.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : false);
        config.setUpdatedBy(updatedBy);
        
        invalidateCache();
        return siteConfigRepository.save(config);
    }
    
    @Override
    @Transactional
    public void deleteConfig(String key) {
        if (!siteConfigRepository.existsByConfigKey(key)) {
            throw new RuntimeException("Config not found: " + key);
        }
        siteConfigRepository.deleteByConfigKey(key);
        invalidateCache();
    }
    
    @Override
    @Transactional
    public void bulkUpdateConfigs(java.util.Map<String, String> configs, User updatedBy) {
        for (java.util.Map.Entry<String, String> entry : configs.entrySet()) {
            ConfigCatalog.Definition definition = ConfigCatalog.find(entry.getKey())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Unknown configuration key: " + entry.getKey()
                    ));
            SiteConfig config = siteConfigRepository.findByConfigKey(entry.getKey()).orElseGet(() -> {
                SiteConfig created = new SiteConfig();
                created.setConfigKey(definition.key());
                created.setConfigType(definition.type());
                created.setCategory(definition.category());
                created.setDescription(definition.description());
                created.setIsPublic(definition.isPublic());
                return created;
            });
                config.setConfigValue(entry.getValue());
                config.setUpdatedBy(updatedBy);
                siteConfigRepository.save(config);
        }
        invalidateCache();
    }
    
    @Override
    public void invalidateCache() {
        cachedConfig = null;
        cacheExpiry = null;
    }
    
    @Override
    public String generateVersionHash() {
        try {
            List<SiteConfig> configs = siteConfigRepository.findAll();
            String content = configs.stream()
                    .sorted(Comparator.comparing(SiteConfig::getConfigKey))
                    .map(c -> c.getConfigKey() + ":" + c.getConfigValue())
                    .collect(Collectors.joining("|"));
            
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < Math.min(8, hash.length); i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(System.currentTimeMillis());
        }
    }
    
    // Helper methods
    private Boolean parseBoolean(String value) {
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }
    
    private String emptyToNull(String value) {
        return (value == null || value.isEmpty()) ? null : value;
    }
    
    private SiteConfigDetailResponse toDetailResponse(SiteConfig config) {
        return SiteConfigDetailResponse.builder()
                .id(config.getId())
                .key(config.getConfigKey())
                .value(config.getConfigValue())
                .type(config.getConfigType())
                .category(config.getCategory())
                .description(config.getDescription())
                .isPublic(config.getIsPublic())
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .build();
    }
    
    private NavigationItemResponse toNavigationItemResponse(NavigationConfig nav) {
        return NavigationItemResponse.builder()
                .key(nav.getTabKey())
                .labelKey(nav.getLabelKey())
                .icon(nav.getIcon())
                .route(nav.getRoute())
                .enabled(nav.getIsEnabled())
                .requiredRole(nav.getRequiredRole())
                .sortOrder(nav.getSortOrder())
                .build();
    }
}
