package icpmapp.config;

import icpmapp.entities.NavigationConfig;
import icpmapp.entities.Role;
import icpmapp.entities.SiteConfig;
import icpmapp.entities.User;
import icpmapp.repository.NavigationConfigRepository;
import icpmapp.repository.SiteConfigRepository;
import icpmapp.repository.UserRepository;
import icpmapp.services.ConfigService;
import icpmapp.services.SetupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class PopulatedDatabaseStartupTest {

    private static final Path DATABASE_BASE = Path.of(
        System.getProperty("java.io.tmpdir"),
        "conferia-populated-" + System.nanoTime()
    );

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add(
            "spring.datasource.url",
            () -> "jdbc:h2:file:" + DATABASE_BASE
        );
        registry.add("conferia.setup-token", () -> "test-setup-token");
        registry.add("debug", () -> "false");
        registry.add("logging.level.root", () -> "WARN");
        registry.add(
            "conferia.jwt-secret",
            () -> "8HMAnIuSaJewPZpV2ah13uLOmySq7wE+kpmRWffu0rg="
        );
    }

    @Autowired
    private UserRepository users;

    @Autowired
    private SiteConfigRepository configs;

    @Autowired
    private NavigationConfigRepository navigation;

    @Autowired
    private SetupService setupService;

    @Autowired
    private ConfigService configService;

    @Test
    void populatedCompatibleDatabaseBypassesSetupAndReturnsStoredConfiguration() {
        User administrator = new User();
        administrator.setEmail("existing-admin@example.test");
        administrator.setFirstname("Existing");
        administrator.setLastname("Administrator");
        administrator.setPassword("encoded");
        administrator.setRole(Role.ADMIN);
        users.save(administrator);

        configs.saveAll(List.of(
            config("conference.name", "Stored Conference"),
            config("conference.dates.start", "2030-06-10"),
            config("conference.dates.end", "2030-06-12"),
            config("conference.location", "Stored Venue"),
            config("conference.timezone", "Europe/Berlin")
        ));
        NavigationConfig home = new NavigationConfig(
            "home",
            "navigation.home",
            "home-outline",
            "/tabs/home",
            1
        );
        home.setIsEnabled(true);
        navigation.save(home);

        var status = setupService.getStatus();

        assertFalse(status.isSetupRequired());
        assertTrue(status.isConfigured());
        assertEquals(
            "Stored Conference",
            configService.getPublicSiteConfig().getConference().getName()
        );
    }

    private SiteConfig config(String key, String value) {
        return new SiteConfig(key, value, "text", "conference", true);
    }
}
