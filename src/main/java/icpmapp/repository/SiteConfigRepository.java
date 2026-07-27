package icpmapp.repository;

import icpmapp.entities.SiteConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SiteConfigRepository extends JpaRepository<SiteConfig, UUID> {
    
    Optional<SiteConfig> findByConfigKey(String configKey);
    
    boolean existsByConfigKey(String configKey);
    
    List<SiteConfig> findByIsPublicTrue();
    
    List<SiteConfig> findByCategory(String category);
    
    List<SiteConfig> findByCategoryAndIsPublicTrue(String category);
    
    @Query("SELECT s FROM SiteConfig s WHERE s.configKey LIKE :prefix%")
    List<SiteConfig> findByConfigKeyStartingWith(String prefix);
    
    @Query("SELECT s FROM SiteConfig s WHERE s.configKey LIKE :prefix% AND s.isPublic = true")
    List<SiteConfig> findPublicByConfigKeyStartingWith(String prefix);
    
    void deleteByConfigKey(String configKey);
}
