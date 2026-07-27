package icpmapp.services;

import icpmapp.dto.requests.NavigationConfigRequest;
import icpmapp.dto.requests.NavigationReorderRequest;
import icpmapp.dto.responses.NavigationConfigDetailResponse;
import icpmapp.dto.responses.NavigationItemResponse;
import icpmapp.entities.NavigationConfig;

import java.util.List;

public interface NavigationService {
    
    // Public endpoints
    List<NavigationItemResponse> getEnabledNavigation();
    
    // Admin endpoints
    List<NavigationConfigDetailResponse> getAllNavigation();
    NavigationConfig updateNavigation(String tabKey, NavigationConfigRequest request);
    void reorderNavigation(NavigationReorderRequest request);
}
