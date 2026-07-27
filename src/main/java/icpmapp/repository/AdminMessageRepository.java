package icpmapp.repository;

import icpmapp.entities.AdminMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminMessageRepository extends JpaRepository<AdminMessage, String> {

    Page<AdminMessage> findAllByOrderBySentAtDesc(Pageable pageable);

    @Query("SELECT COUNT(am) FROM AdminMessage am")
    long countAllMessages();
}
