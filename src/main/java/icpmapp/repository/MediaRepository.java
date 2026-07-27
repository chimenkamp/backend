package icpmapp.repository;

import icpmapp.entities.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MediaRepository extends JpaRepository<Media, UUID> {
    
    List<Media> findByCategory(String category);
    
    List<Media> findByMimeTypeStartingWith(String mimeTypePrefix);
    
    List<Media> findAllByOrderByCreatedAtDesc();

    Optional<Media> findByFilename(String filename);
}
