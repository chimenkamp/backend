package icpmapp.config;

import icpmapp.repository.LocalizedContentRepository;
import icpmapp.repository.NavigationConfigRepository;
import icpmapp.repository.PageRepository;
import icpmapp.repository.SessionHeaderRepository;
import icpmapp.repository.SiteConfigRepository;
import icpmapp.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class EmptyDatabaseStartupTest {

    private static final Path DATABASE_BASE = Path.of(
        System.getProperty("java.io.tmpdir"),
        "conferia-empty-" + System.nanoTime()
    );

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add(
            "spring.datasource.url",
            () -> "jdbc:h2:file:" + DATABASE_BASE
        );
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
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
    private PageRepository pages;

    @Autowired
    private SessionHeaderRepository sessions;

    @Autowired
    private LocalizedContentRepository localizedContent;

    @Test
    void createsSchemaWithoutConferenceRecords() {
        assertEquals(0, users.count());
        assertEquals(0, configs.count());
        assertEquals(0, navigation.count());
        assertEquals(0, pages.count());
        assertEquals(0, sessions.count());
        assertEquals(0, localizedContent.count());
    }
}
