package icpmapp.repository;

import icpmapp.dto.SessionHeaderDTO;
import icpmapp.entities.SessionHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;


import java.util.List;

@Repository
public interface SessionHeaderRepository extends JpaRepository<SessionHeader, Long> {
    // In your SessionHeaderRepository
    List<SessionHeader> findByLikes_Id(Integer userId);

    @Query("SELECT sh.id FROM SessionHeader sh JOIN sh.likes u WHERE u.id = :userId")
    List<Long> findSessionIdsLikedByUser(@Param("userId") Integer userId);

    @Modifying
    @Query(value = "DELETE FROM session_likes WHERE user_id = :sessionId", nativeQuery = true)
    void deleteAllLikesForSession(@Param("sessionId") Long sessionId);

    @Query("""
        SELECT new icpmapp.dto.SessionHeaderDTO(
            sh.id,
            sh.name,
            sh.host,
            sh.location,
            sh.startTime,
            sh.endTime,
            sh.type,
            count(u),
            track.id,
            track.name,
            track.color
        )
        FROM SessionHeader sh
        LEFT JOIN sh.likes u
        LEFT JOIN sh.track track
        WHERE sh.isPublished = true
        GROUP BY sh.id, track.id
        ORDER BY sh.startTime
        """)
    List<SessionHeaderDTO> findPublishedSessionsWithLikes();

}
