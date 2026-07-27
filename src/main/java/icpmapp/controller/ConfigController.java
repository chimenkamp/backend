package icpmapp.controller;

import icpmapp.dto.responses.ConfigVersionResponse;
import icpmapp.dto.responses.PublicSiteConfigResponse;
import icpmapp.services.ConfigService;
import icpmapp.services.LocaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/config")
@RequiredArgsConstructor
public class ConfigController {
    
    private final ConfigService configService;
    private final LocaleService localeService;
    
    /**
     * GET /api/v1/config/site
     * Returns all public site configuration for app initialization.
     */
    @GetMapping("/site")
    public ResponseEntity<PublicSiteConfigResponse> getSiteConfig() {
        return ResponseEntity.ok(configService.getPublicSiteConfig());
    }
    
    /**
     * GET /api/v1/config/locales/{languageCode}
     * Returns localized content overrides for a specific language.
     */
    @GetMapping("/locales/{languageCode}")
    public ResponseEntity<Map<String, String>> getLocaleContent(@PathVariable String languageCode) {
        return ResponseEntity.ok(localeService.getLocaleContent(languageCode));
    }
    
    /**
     * GET /api/v1/config/version
     * Returns current config version hash for cache invalidation.
     */
    @GetMapping("/version")
    public ResponseEntity<ConfigVersionResponse> getConfigVersion() {
        return ResponseEntity.ok(configService.getConfigVersion());
    }
}
