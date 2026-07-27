package icpmapp.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicSiteConfigResponse {
    private ConferenceConfig conference;
    private BrandingConfig branding;
    private FeaturesConfig features;
    private List<NavigationItemResponse> navigation;
    private SocialConfig social;
    private ContactConfig contact;
    private String version;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConferenceConfig {
        private String name;
        private String tagline;
        private String description;
        private DateRange dates;
        private String location;
        private String timezone;
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class DateRange {
            private String start;
            private String end;
        }
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BrandingConfig {
        private String logoLight;
        private String logoDark;
        private String homeImage;
        private String primaryColor;
        private String secondaryColor;
        private String accentColor;
        private String favicon;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FeaturesConfig {
        private Boolean galleryEnabled;
        private Boolean attendeesVisible;
        private Boolean messagesEnabled;
        private Boolean registrationOpen;
        private Boolean agendaEnabled;
        private Boolean myGalleryEnabled;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SocialConfig {
        private String twitterUrl;
        private String linkedinUrl;
        private String websiteUrl;
        private String registrationUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContactConfig {
        private String email;
        private String name;
        private String organization;
        private String phone;
    }
}
