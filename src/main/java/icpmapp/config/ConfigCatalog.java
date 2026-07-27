package icpmapp.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Defines the product-level configuration schema without conference data.
 */
public final class ConfigCatalog {

    public record Definition(
        String key,
        String type,
        String category,
        String description,
        boolean isPublic,
        String defaultValue
    ) {
    }

    private static final List<Definition> DEFINITIONS = List.of(
        text("conference.name", "conference", "Conference name", ""),
        text("conference.tagline", "conference", "Conference tagline", ""),
        html("conference.description", "conference", "Conference description", ""),
        text("conference.dates.start", "conference", "Conference start date in ISO format", ""),
        text("conference.dates.end", "conference", "Conference end date in ISO format", ""),
        text("conference.location", "conference", "Conference venue", ""),
        text("conference.timezone", "conference", "Conference IANA timezone", ""),

        url("branding.logoLight", "branding", "Logo for light backgrounds", ""),
        url("branding.logoDark", "branding", "Logo for dark backgrounds", ""),
        url("branding.homeImage", "branding", "Home page hero image", ""),
        url("branding.favicon", "branding", "Favicon", ""),
        color("branding.primaryColor", "branding", "Primary color", "#3880ff"),
        color("branding.secondaryColor", "branding", "Secondary color", "#3dc2ff"),
        color("branding.accentColor", "branding", "Accent color", "#5260ff"),

        bool("features.galleryEnabled", "features", "Enable the gallery", false),
        bool("features.attendeesVisible", "features", "Show the attendee directory", false),
        bool("features.messagesEnabled", "features", "Enable messages", false),
        bool("features.registrationOpen", "features", "Allow registration", false),
        bool("features.agendaEnabled", "features", "Enable the agenda", false),
        bool("features.myGalleryEnabled", "features", "Enable personal galleries", false),

        url("social.website", "social", "Conference website", ""),
        url("social.twitter", "social", "Twitter or X profile", ""),
        url("social.linkedin", "social", "LinkedIn profile", ""),
        url("social.registration", "social", "Registration website", ""),

        text("contact.email", "contact", "Contact email", ""),
        text("contact.name", "contact", "Contact name", ""),
        text("contact.organization", "contact", "Organizing institution", ""),
        text("contact.phone", "contact", "Contact phone number", "")
    );

    private static final Map<String, Definition> BY_KEY;

    static {
        Map<String, Definition> definitions = new LinkedHashMap<>();
        DEFINITIONS.forEach(definition -> definitions.put(definition.key(), definition));
        BY_KEY = Map.copyOf(definitions);
    }

    private ConfigCatalog() {
    }

    public static List<Definition> definitions() {
        return DEFINITIONS;
    }

    public static Optional<Definition> find(String key) {
        return Optional.ofNullable(BY_KEY.get(key));
    }

    private static Definition text(String key, String category, String description, String defaultValue) {
        return definition(key, "text", category, description, defaultValue);
    }

    private static Definition html(String key, String category, String description, String defaultValue) {
        return definition(key, "html", category, description, defaultValue);
    }

    private static Definition url(String key, String category, String description, String defaultValue) {
        return definition(key, "url", category, description, defaultValue);
    }

    private static Definition color(String key, String category, String description, String defaultValue) {
        return definition(key, "color", category, description, defaultValue);
    }

    private static Definition bool(String key, String category, String description, boolean defaultValue) {
        return definition(key, "boolean", category, description, Boolean.toString(defaultValue));
    }

    private static Definition definition(
        String key,
        String type,
        String category,
        String description,
        String defaultValue
    ) {
        return new Definition(key, type, category, description, true, defaultValue);
    }
}
