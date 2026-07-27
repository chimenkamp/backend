package icpmapp.services;

import icpmapp.entities.SiteConfig;
import icpmapp.entities.User;
import icpmapp.dto.responses.PublicSiteConfigResponse;
import icpmapp.repository.NavigationConfigRepository;
import icpmapp.repository.SiteConfigRepository;
import icpmapp.services.impl.ConfigServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConfigServiceImplTest {

    @Test
    void exposesCompletePublicConferenceConfiguration() {
        SiteConfigRepository configs = mock(SiteConfigRepository.class);
        NavigationConfigRepository navigation = mock(NavigationConfigRepository.class);
        ConfigService service = new ConfigServiceImpl(configs, navigation);
        when(configs.findByIsPublicTrue()).thenReturn(List.of(
            config("conference.tagline", "Tagline"),
            config("branding.accentColor", "#123456"),
            config("features.agendaEnabled", "true"),
            config("features.myGalleryEnabled", "true"),
            config("social.registration", "https://registration.example"),
            config("contact.email", "contact@example.test")
        ));
        when(configs.findAll()).thenReturn(List.of());

        PublicSiteConfigResponse response = service.getPublicSiteConfig();

        assertEquals("Tagline", response.getConference().getTagline());
        assertEquals("#123456", response.getBranding().getAccentColor());
        assertEquals(true, response.getFeatures().getAgendaEnabled());
        assertEquals(true, response.getFeatures().getMyGalleryEnabled());
        assertEquals("https://registration.example", response.getSocial().getRegistrationUrl());
        assertEquals("contact@example.test", response.getContact().getEmail());
    }

    @Test
    void bulkUpdateCreatesMissingRecognizedConfigurationKeys() {
        SiteConfigRepository configs = mock(SiteConfigRepository.class);
        NavigationConfigRepository navigation = mock(NavigationConfigRepository.class);
        ConfigService service = new ConfigServiceImpl(configs, navigation);
        User administrator = new User();
        when(configs.findByConfigKey("conference.name")).thenReturn(Optional.empty());

        service.bulkUpdateConfigs(Map.of("conference.name", "New Conference"), administrator);

        ArgumentCaptor<SiteConfig> captor = ArgumentCaptor.forClass(SiteConfig.class);
        verify(configs).save(captor.capture());
        assertEquals("conference.name", captor.getValue().getConfigKey());
        assertEquals("New Conference", captor.getValue().getConfigValue());
        assertEquals(administrator, captor.getValue().getUpdatedBy());
    }

    @Test
    void bulkUpdateRejectsUnknownConfigurationKeys() {
        SiteConfigRepository configs = mock(SiteConfigRepository.class);
        NavigationConfigRepository navigation = mock(NavigationConfigRepository.class);
        ConfigService service = new ConfigServiceImpl(configs, navigation);

        assertThrows(
            IllegalArgumentException.class,
            () -> service.bulkUpdateConfigs(Map.of("instance.hardcoded", "value"), new User())
        );
    }

    private SiteConfig config(String key, String value) {
        SiteConfig config = new SiteConfig();
        config.setConfigKey(key);
        config.setConfigValue(value);
        config.setIsPublic(true);
        return config;
    }
}
