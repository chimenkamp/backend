package icpmapp.repository;

import icpmapp.entities.LocalizedContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LocalizedContentRepository extends JpaRepository<LocalizedContent, UUID> {
    
    List<LocalizedContent> findByLanguageCode(String languageCode);
    
    Optional<LocalizedContent> findByContentKeyAndLanguageCode(String contentKey, String languageCode);
    
    boolean existsByContentKeyAndLanguageCode(String contentKey, String languageCode);
    
    void deleteByContentKeyAndLanguageCode(String contentKey, String languageCode);
    
    @Query("SELECT DISTINCT l.languageCode FROM LocalizedContent l")
    List<String> findDistinctLanguageCodes();
    
    @Query("SELECT COUNT(l) FROM LocalizedContent l WHERE l.languageCode = :languageCode")
    Long countByLanguageCode(@Param("languageCode") String languageCode);
}
