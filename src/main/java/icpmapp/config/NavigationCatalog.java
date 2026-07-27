package icpmapp.config;

import java.util.List;

/**
 * Defines the routes supported by the generic frontend.
 */
public final class NavigationCatalog {

    public record Definition(
        String key,
        String labelKey,
        String icon,
        String route,
        int sortOrder,
        boolean enabled,
        String requiredRole
    ) {
    }

    private static final List<Definition> DEFINITIONS = List.of(
        new Definition("home", "navigation.home", "home-outline", "/tabs/home", 1, true, null),
        new Definition("calendar", "navigation.calendar", "calendar-outline", "/tabs/calendar", 2, false, null),
        new Definition("agenda", "navigation.agenda", "list-outline", "/tabs/agenda", 3, false, "user"),
        new Definition("attendees", "navigation.attendees", "people-outline", "/tabs/attendees", 4, false, "user"),
        new Definition("gallery", "navigation.gallery", "images-outline", "/tabs/gallery", 5, false, "user"),
        new Definition("myGallery", "navigation.myGallery", "camera-outline", "/tabs/my-gallery", 6, false, "user"),
        new Definition("messages", "navigation.messages", "mail-outline", "/tabs/messages", 7, false, "user"),
        new Definition("about", "navigation.about", "information-circle-outline", "/tabs/about", 8, true, null)
    );

    private NavigationCatalog() {
    }

    public static List<Definition> definitions() {
        return DEFINITIONS;
    }
}
