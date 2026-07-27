package icpmapp.repository;

import icpmapp.entities.NavigationConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NavigationConfigRepository extends JpaRepository<NavigationConfig, UUID> {
    
    Optional<NavigationConfig> findByTabKey(String tabKey);
    
    boolean existsByTabKey(String tabKey);
    
    List<NavigationConfig> findByIsEnabledTrueOrderBySortOrderAsc();
    
    List<NavigationConfig> findAllByOrderBySortOrderAsc();
}
