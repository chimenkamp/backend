package icpmapp.services.impl;

import icpmapp.dto.requests.NavigationConfigRequest;
import icpmapp.dto.requests.NavigationReorderRequest;
import icpmapp.dto.responses.NavigationConfigDetailResponse;
import icpmapp.dto.responses.NavigationItemResponse;
import icpmapp.entities.NavigationConfig;
import icpmapp.repository.NavigationConfigRepository;
import icpmapp.services.ConfigService;
import icpmapp.services.NavigationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NavigationServiceImpl implements NavigationService {
    
    private final NavigationConfigRepository navigationConfigRepository;
    private final ConfigService configService;
    
    @Override
    public List<NavigationItemResponse> getEnabledNavigation() {
        return navigationConfigRepository.findByIsEnabledTrueOrderBySortOrderAsc().stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<NavigationConfigDetailResponse> getAllNavigation() {
        return navigationConfigRepository.findAllByOrderBySortOrderAsc().stream()
                .map(this::toDetailResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public NavigationConfig updateNavigation(String tabKey, NavigationConfigRequest request) {
        NavigationConfig nav = navigationConfigRepository.findByTabKey(tabKey)
                .orElseThrow(() -> new RuntimeException("Navigation item not found: " + tabKey));
        
        if (request.getLabelKey() != null) nav.setLabelKey(request.getLabelKey());
        if (request.getIcon() != null) nav.setIcon(request.getIcon());
        if (request.getRoute() != null) nav.setRoute(request.getRoute());
        if (request.getSortOrder() != null) nav.setSortOrder(request.getSortOrder());
        if (request.getIsEnabled() != null) nav.setIsEnabled(request.getIsEnabled());
        if (request.getRequiredRole() != null) {
            nav.setRequiredRole(request.getRequiredRole().isEmpty() ? null : request.getRequiredRole());
        }
        
        NavigationConfig saved = navigationConfigRepository.save(nav);
        configService.invalidateCache(); // Invalidate site config cache
        return saved;
    }
    
    @Override
    @Transactional
    public void reorderNavigation(NavigationReorderRequest request) {
        for (NavigationReorderRequest.NavigationOrderItem item : request.getNavigation()) {
            // Use tabKey for lookup (id is optional)
            navigationConfigRepository.findByTabKey(item.getTabKey()).ifPresent(nav -> {
                nav.setSortOrder(item.getSortOrder());
                nav.setIsEnabled(item.getIsEnabled());
                nav.setLabelKey(item.getLabelKey());
                nav.setIcon(item.getIcon());
                nav.setRoute(item.getRoute());
                nav.setRequiredRole(item.getRequiredRole());
                navigationConfigRepository.save(nav);
            });
        }
        configService.invalidateCache(); // Invalidate site config cache
    }
    
    // Helper methods
    private NavigationItemResponse toItemResponse(NavigationConfig nav) {
        return NavigationItemResponse.builder()
                .key(nav.getTabKey())
                .labelKey(nav.getLabelKey())
                .icon(nav.getIcon())
                .route(nav.getRoute())
                .enabled(nav.getIsEnabled())
                .requiredRole(nav.getRequiredRole())
                .build();
    }
    
    private NavigationConfigDetailResponse toDetailResponse(NavigationConfig nav) {
        return NavigationConfigDetailResponse.builder()
                .id(nav.getId())
                .tabKey(nav.getTabKey())
                .labelKey(nav.getLabelKey())
                .icon(nav.getIcon())
                .route(nav.getRoute())
                .sortOrder(nav.getSortOrder())
                .isEnabled(nav.getIsEnabled())
                .requiredRole(nav.getRequiredRole())
                .createdAt(nav.getCreatedAt())
                .updatedAt(nav.getUpdatedAt())
                .build();
    }
}
