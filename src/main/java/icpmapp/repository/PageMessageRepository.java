package icpmapp.repository;

import icpmapp.entities.PageMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PageMessageRepository extends JpaRepository<PageMessage, UUID> {
    
    List<PageMessage> findByPageIdOrderBySortOrderAsc(UUID pageId);
    
    void deleteByPageId(UUID pageId);
}
