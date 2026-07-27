package icpmapp.services.impl;

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
import icpmapp.services.JWTService;
import icpmapp.services.SetupService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SetupServiceImpl implements SetupService {

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final UserRepository userRepository;
    private final SiteConfigRepository siteConfigRepository;
    private final NavigationConfigRepository navigationConfigRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final JdbcTemplate jdbcTemplate;
    private final String setupToken;

    public SetupServiceImpl(
        UserRepository userRepository,
        SiteConfigRepository siteConfigRepository,
        NavigationConfigRepository navigationConfigRepository,
        PasswordEncoder passwordEncoder,
        JWTService jwtService,
        JdbcTemplate jdbcTemplate,
        @Value("${conferia.setup-token:}") String setupToken
    ) {
        this.userRepository = userRepository;
        this.siteConfigRepository = siteConfigRepository;
        this.navigationConfigRepository = navigationConfigRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jdbcTemplate = jdbcTemplate;
        this.setupToken = setupToken == null ? "" : setupToken;
    }

    @Override
    @Transactional(readOnly = true)
    public SetupStatusResponse getStatus() {
        boolean hasAdministrator = userRepository.existsByRole(Role.ADMIN);
        boolean setupRequired = !hasAdministrator;
        return SetupStatusResponse.builder()
            .setupRequired(setupRequired)
            .setupBlocked(setupRequired && setupToken.isBlank())
            .configured(hasAdministrator && hasRequiredConfiguration())
            .build();
    }

    @Override
    @Transactional
    public synchronized JwtAuthenticationResponse bootstrap(BootstrapSetupRequest request) {
        jdbcTemplate.queryForObject(
            "SELECT id FROM setup_lock WHERE id = 1 FOR UPDATE",
            Integer.class
        );
        if (userRepository.existsByRole(Role.ADMIN)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Setup has already been completed");
        }
        if (setupToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Setup token is not configured");
        }
        validateRequest(request);
        if (!tokensMatch(request.getSetupToken(), setupToken)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid setup token");
        }

        User administrator = new User();
        administrator.setEmail(request.getEmail().trim().toLowerCase());
        administrator.setPassword(passwordEncoder.encode(request.getPassword()));
        administrator.setFirstname(request.getFirstname().trim());
        administrator.setLastname(request.getLastname().trim());
        administrator.setRole(Role.ADMIN);
        administrator.setSharingchoice(false);
        administrator.setIsActive(true);
        User savedAdministrator = userRepository.save(administrator);

        createMissingConfiguration(savedAdministrator);
        createMissingNavigation();

        JwtAuthenticationResponse response = new JwtAuthenticationResponse();
        response.setAccessToken(jwtService.generateToken(savedAdministrator));
        response.setRefreshToken(jwtService.generateRefreshToken(new HashMap<>(), savedAdministrator));
        response.setUserId(savedAdministrator.getId());
        return response;
    }

    private void validateRequest(BootstrapSetupRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Setup request is required");
        }
        if (isBlank(request.getSetupToken())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Setup token is required");
        }
        if (isBlank(request.getEmail()) || !EMAIL_PATTERN.matcher(request.getEmail().trim()).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A valid administrator email is required");
        }
        if (request.getPassword() == null || request.getPassword().length() < 12) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Administrator password must contain at least 12 characters"
            );
        }
        if (isBlank(request.getFirstname()) || isBlank(request.getLastname())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Administrator first name and last name are required"
            );
        }
    }

    private boolean tokensMatch(String provided, String expected) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] providedHash = digest.digest(provided.getBytes(StandardCharsets.UTF_8));
            byte[] expectedHash = digest.digest(expected.getBytes(StandardCharsets.UTF_8));
            return MessageDigest.isEqual(providedHash, expectedHash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private void createMissingConfiguration(User administrator) {
        List<SiteConfig> configs = new ArrayList<>();
        for (ConfigCatalog.Definition definition : ConfigCatalog.definitions()) {
            if (siteConfigRepository.findByConfigKey(definition.key()).isPresent()) {
                continue;
            }
            SiteConfig config = new SiteConfig();
            config.setConfigKey(definition.key());
            config.setConfigValue(definition.defaultValue());
            config.setConfigType(definition.type());
            config.setCategory(definition.category());
            config.setDescription(definition.description());
            config.setIsPublic(definition.isPublic());
            config.setUpdatedBy(administrator);
            configs.add(config);
        }
        siteConfigRepository.saveAll(configs);
    }

    private void createMissingNavigation() {
        List<NavigationConfig> items = new ArrayList<>();
        for (NavigationCatalog.Definition definition : NavigationCatalog.definitions()) {
            if (navigationConfigRepository.findByTabKey(definition.key()).isPresent()) {
                continue;
            }
            NavigationConfig item = new NavigationConfig();
            item.setTabKey(definition.key());
            item.setLabelKey(definition.labelKey());
            item.setIcon(definition.icon());
            item.setRoute(definition.route());
            item.setSortOrder(definition.sortOrder());
            item.setIsEnabled(definition.enabled());
            item.setRequiredRole(definition.requiredRole());
            items.add(item);
        }
        navigationConfigRepository.saveAll(items);
    }

    private boolean hasRequiredConfiguration() {
        Map<String, String> configs = siteConfigRepository.findAll().stream()
            .collect(Collectors.toMap(
                SiteConfig::getConfigKey,
                config -> config.getConfigValue() == null ? "" : config.getConfigValue(),
                (first, second) -> second
            ));

        if (isBlank(configs.get("conference.name"))
            || isBlank(configs.get("conference.location"))
            || isBlank(configs.get("conference.timezone"))
            || isBlank(configs.get("conference.dates.start"))
            || isBlank(configs.get("conference.dates.end"))) {
            return false;
        }

        try {
            LocalDate start = LocalDate.parse(configs.get("conference.dates.start"));
            LocalDate end = LocalDate.parse(configs.get("conference.dates.end"));
            if (end.isBefore(start)) {
                return false;
            }
            ZoneId.of(configs.get("conference.timezone"));
        } catch (RuntimeException exception) {
            return false;
        }

        return !navigationConfigRepository.findByIsEnabledTrueOrderBySortOrderAsc().isEmpty();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
