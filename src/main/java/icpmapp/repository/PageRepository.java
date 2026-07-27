package icpmapp.repository;

import icpmapp.entities.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PageRepository extends JpaRepository<Page, UUID> {
    
    Optional<Page> findBySlug(String slug);
    
    boolean existsBySlug(String slug);
    
    List<Page> findByIsPublishedTrueOrderBySortOrderAsc();
    
    List<Page> findByIsPublishedTrueAndIsPublicTrueOrderBySortOrderAsc();
    
    @Query("SELECT p FROM Page p WHERE p.isPublished = :published ORDER BY p.sortOrder ASC")
    List<Page> findByIsPublished(@Param("published") Boolean published);
    
    @Query("SELECT p FROM Page p ORDER BY p.sortOrder ASC")
    List<Page> findAllOrderBySortOrderAsc();
    
    @Query(value = "SELECT * FROM pages p WHERE " +
           "LOWER(p.title) LIKE :search OR " +
           "LOWER(p.content) LIKE :search " +
           "ORDER BY p.sort_order ASC", nativeQuery = true)
    List<Page> searchByTitleOrContent(@Param("search") String search);
    
    @Query(value = "SELECT * FROM pages p WHERE " +
           "(:search IS NULL OR LOWER(p.title) LIKE :search OR " +
           "LOWER(p.content) LIKE :search) " +
           "AND (:published IS NULL OR p.is_published = :published) " +
           "ORDER BY p.sort_order ASC", nativeQuery = true)
    List<Page> findByFilters(@Param("search") String search, @Param("published") Boolean published);
}

