package icpmapp.services;

import icpmapp.config.ConfigCatalog;
import icpmapp.config.NavigationCatalog;
import icpmapp.dto.requests.BootstrapSetupRequest;
import icpmapp.dto.requests.JwtAuthenticationResponse;
import icpmapp.dto.responses.SetupStatusResponse;
import icpmapp.entities.NavigationConfig;
import icpmapp.entities.Role;
import icpmapp.entities.SiteConfig;
import icpmapp.entities.User;
import icpmapp.repository.NavigationConfigRepository;
import icpmapp.repository.SiteConfigRepository;
import icpmapp.repository.UserRepository;
import icpmapp.services.impl.SetupServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SetupServiceImplTest {

    private UserRepository users;
    private SiteConfigRepository configs;
    private NavigationConfigRepository navigation;
    private PasswordEncoder passwordEncoder;
    private JWTService jwtService;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        users = mock(UserRepository.class);
        configs = mock(SiteConfigRepository.class);
        navigation = mock(NavigationConfigRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtService = mock(JWTService.class);
        jdbcTemplate = mock(JdbcTemplate.class);
    }

    @Test
    void reportsBlockedSetupWhenDatabaseHasNoAdministratorAndNoSetupToken() {
        SetupService service = serviceWithToken("");
        when(users.existsByRole(Role.ADMIN)).thenReturn(false);

        SetupStatusResponse status = service.getStatus();

        assertTrue(status.isSetupRequired());
        assertTrue(status.isSetupBlocked());
        assertFalse(status.isConfigured());
    }

    @Test
    void reportsConfiguredOnlyWhenRequiredConferenceValuesAndNavigationExist() {
        SetupService service = serviceWithToken("setup-secret");
        when(users.existsByRole(Role.ADMIN)).thenReturn(true);
        when(configs.findAll()).thenReturn(List.of(
            config("conference.name", "Conference"),
            config("conference.dates.start", "2030-06-10"),
            config("conference.dates.end", "2030-06-12"),
            config("conference.location", "Venue"),
            config("conference.timezone", "Europe/Berlin")
        ));
        NavigationConfig home = new NavigationConfig();
        home.setIsEnabled(true);
        when(navigation.findByIsEnabledTrueOrderBySortOrderAsc()).thenReturn(List.of(home));

        SetupStatusResponse status = service.getStatus();

        assertFalse(status.isSetupRequired());
        assertFalse(status.isSetupBlocked());
        assertTrue(status.isConfigured());
    }

    @Test
    void reportsUnconfiguredForInvalidDateRangeOrTimezone() {
        SetupService service = serviceWithToken("setup-secret");
        when(users.existsByRole(Role.ADMIN)).thenReturn(true);
        when(configs.findAll()).thenReturn(List.of(
            config("conference.name", "Conference"),
            config("conference.dates.start", "2030-06-12"),
            config("conference.dates.end", "2030-06-10"),
            config("conference.location", "Venue"),
            config("conference.timezone", "Not/A_Timezone")
        ));

        SetupStatusResponse status = service.getStatus();

        assertFalse(status.isConfigured());
    }

    @Test
    void rejectsInvalidSetupTokenWithoutCreatingAdministrator() {
        SetupService service = serviceWithToken("correct-token");
        when(users.existsByRole(Role.ADMIN)).thenReturn(false);

        BootstrapSetupRequest request = validRequest();
        request.setSetupToken("wrong-token");

        assertThrows(ResponseStatusException.class, () -> service.bootstrap(request));
        verify(users, never()).save(any(User.class));
    }

    @Test
    void rejectsPasswordsShorterThanTwelveCharacters() {
        SetupService service = serviceWithToken("correct-token");
        when(users.existsByRole(Role.ADMIN)).thenReturn(false);
        BootstrapSetupRequest request = validRequest();
        request.setPassword("too-short");

        ResponseStatusException exception =
            assertThrows(ResponseStatusException.class, () -> service.bootstrap(request));

        assertEquals(400, exception.getStatusCode().value());
        verify(users, never()).save(any(User.class));
    }

    @Test
    void rejectsInvalidAdministratorEmail() {
        SetupService service = serviceWithToken("correct-token");
        when(users.existsByRole(Role.ADMIN)).thenReturn(false);
        BootstrapSetupRequest request = validRequest();
        request.setEmail("not-an-email");

        ResponseStatusException exception =
            assertThrows(ResponseStatusException.class, () -> service.bootstrap(request));

        assertEquals(400, exception.getStatusCode().value());
        verify(users, never()).save(any(User.class));
    }

    @Test
    void bootstrapCreatesOneAdministratorAndGenericCatalog() {
        SetupService service = serviceWithToken("correct-token");
        when(users.existsByRole(Role.ADMIN)).thenReturn(false);
        when(passwordEncoder.encode("twelve-characters")).thenReturn("encoded");
        when(users.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(42);
            return user;
        });
        when(jwtService.generateToken(any(User.class))).thenReturn("access");
        when(jwtService.generateRefreshToken(any(), any(User.class))).thenReturn("refresh");
        when(configs.saveAll(any())).thenAnswer(invocation -> {
            Iterable<SiteConfig> saved = invocation.getArgument(0);
            List<SiteConfig> values = toList(saved);
            assertEquals(ConfigCatalog.definitions().size(), values.size());
            assertTrue(values.stream()
                .filter(config -> config.getConfigKey().startsWith("conference."))
                .allMatch(config -> config.getConfigValue().isEmpty()));
            return values;
        });
        when(navigation.saveAll(any())).thenAnswer(invocation -> {
            Iterable<NavigationConfig> saved = invocation.getArgument(0);
            List<NavigationConfig> values = toList(saved);
            assertEquals(
                NavigationCatalog.definitions().stream()
                    .map(NavigationCatalog.Definition::key)
                    .collect(java.util.stream.Collectors.toSet()),
                values.stream().map(NavigationConfig::getTabKey).collect(java.util.stream.Collectors.toSet())
            );
            return values;
        });

        JwtAuthenticationResponse response = service.bootstrap(validRequest());

        assertEquals("access", response.getAccessToken());
        assertEquals("refresh", response.getRefreshToken());
        assertEquals(42, response.getUserId());
        verify(users).save(any(User.class));
        verify(configs).saveAll(any());
        verify(navigation).saveAll(any());
    }

    @Test
    void bootstrapCannotRunAfterAdministratorExists() {
        SetupService service = serviceWithToken("correct-token");
        when(users.existsByRole(Role.ADMIN)).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> service.bootstrap(validRequest()));
        verify(users, never()).save(any(User.class));
    }

    @Test
    void concurrentBootstrapCreatesExactlyOneAdministrator() throws Exception {
        SetupService service = serviceWithToken("correct-token");
        AtomicBoolean administratorExists = new AtomicBoolean(false);
        when(users.existsByRole(Role.ADMIN)).thenAnswer(invocation -> administratorExists.get());
        when(users.save(any(User.class))).thenAnswer(invocation -> {
            administratorExists.set(true);
            User user = invocation.getArgument(0);
            user.setId(42);
            return user;
        });
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(jwtService.generateToken(any(User.class))).thenReturn("access");
        when(jwtService.generateRefreshToken(any(), any(User.class))).thenReturn("refresh");
        when(configs.findByConfigKey(any())).thenReturn(Optional.empty());
        when(navigation.findByTabKey(any())).thenReturn(Optional.empty());

        CountDownLatch start = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(2);
        try {
            Future<Integer> first = executor.submit(() -> bootstrapStatus(service, start));
            Future<Integer> second = executor.submit(() -> bootstrapStatus(service, start));
            start.countDown();

            List<Integer> statuses = List.of(first.get(), second.get());
            assertEquals(1, statuses.stream().filter(status -> status == 200).count());
            assertEquals(1, statuses.stream().filter(status -> status == 409).count());
            verify(users).save(any(User.class));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void bootstrapRequestStringDoesNotExposeSecrets() {
        BootstrapSetupRequest request = validRequest();

        assertNotEquals(-1, request.toString().indexOf("admin@example.test"));
        assertEquals(-1, request.toString().indexOf("correct-token"));
        assertEquals(-1, request.toString().indexOf("twelve-characters"));
    }

    private int bootstrapStatus(SetupService service, CountDownLatch start) throws InterruptedException {
        start.await();
        try {
            service.bootstrap(validRequest());
            return 200;
        } catch (ResponseStatusException exception) {
            return exception.getStatusCode().value();
        }
    }

    private SetupService serviceWithToken(String setupToken) {
        return new SetupServiceImpl(
            users,
            configs,
            navigation,
            passwordEncoder,
            jwtService,
            jdbcTemplate,
            setupToken
        );
    }

    private BootstrapSetupRequest validRequest() {
        BootstrapSetupRequest request = new BootstrapSetupRequest();
        request.setSetupToken("correct-token");
        request.setEmail("admin@example.test");
        request.setPassword("twelve-characters");
        request.setFirstname("Admin");
        request.setLastname("User");
        return request;
    }

    private SiteConfig config(String key, String value) {
        SiteConfig config = new SiteConfig();
        config.setConfigKey(key);
        config.setConfigValue(value);
        return config;
    }

    private <T> List<T> toList(Iterable<T> values) {
        java.util.ArrayList<T> result = new java.util.ArrayList<>();
        values.forEach(result::add);
        return result;
    }
}
