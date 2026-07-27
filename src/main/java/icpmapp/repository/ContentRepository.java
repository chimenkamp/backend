package icpmapp.repository;

import icpmapp.entities.Content;
import icpmapp.entities.ContentCategory;
import icpmapp.entities.ContentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContentRepository extends JpaRepository<Content, String> {

    Optional<Content> findByKey(String key);

    List<Content> findByCategory(ContentCategory category);

    List<Content> findByType(ContentType type);

    List<Content> findByCategoryAndType(ContentCategory category, ContentType type);

    @Query(value = "SELECT * FROM content c WHERE " +
            "LOWER(c.content_key) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.content_value) LIKE LOWER(CONCAT('%', :search, '%'))", nativeQuery = true)
    List<Content> searchByKeyOrValue(@Param("search") String search);

    @Query(value = "SELECT * FROM content c WHERE c.category = :category AND " +
            "(LOWER(c.content_key) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.content_value) LIKE LOWER(CONCAT('%', :search, '%')))", nativeQuery = true)
    List<Content> searchByKeyOrValueAndCategory(@Param("search") String search, @Param("category") String category);

    @Query(value = "SELECT * FROM content c WHERE c.type = :type AND " +
            "(LOWER(c.content_key) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.content_value) LIKE LOWER(CONCAT('%', :search, '%')))", nativeQuery = true)
    List<Content> searchByKeyOrValueAndType(@Param("search") String search, @Param("type") String type);

    @Query(value = "SELECT * FROM content c WHERE c.category = :category AND c.type = :type AND " +
            "(LOWER(c.content_key) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.content_value) LIKE LOWER(CONCAT('%', :search, '%')))", nativeQuery = true)
    List<Content> searchByKeyOrValueAndCategoryAndType(@Param("search") String search,
                                                        @Param("category") String category,
                                                        @Param("type") String type);
}
