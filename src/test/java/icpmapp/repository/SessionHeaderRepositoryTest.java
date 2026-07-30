package icpmapp.repository;

import icpmapp.dto.SessionHeaderDTO;
import icpmapp.entities.SessionHeader;
import icpmapp.entities.Track;
import icpmapp.entities.Role;
import icpmapp.entities.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class SessionHeaderRepositoryTest {

    @Autowired
    private SessionHeaderRepository sessions;

    @Autowired
    private TrackRepository tracks;

    @Autowired
    private UserRepository users;

    @Test
    void publicAgendaQueryReturnsOnlyPublishedSessionsWithTrackMetadata() {
        Track research = tracks.save(new Track("Research", "#5e81ac"));
        sessions.save(session("Published research", true, research));
        sessions.save(session("Unpublished draft", false, research));

        List<SessionHeaderDTO> result = sessions.findPublishedSessionsWithLikes();

        assertEquals(1, result.size());
        assertEquals("Published research", result.get(0).getName());
        assertEquals(research.getId(), result.get(0).getTrackId());
        assertEquals("Research", result.get(0).getTrackName());
        assertEquals("#5e81ac", result.get(0).getTrackColor());
    }

    @Test
    void removesPersonalAgendaLinksBeforeDeletingASession() {
        Track research = tracks.save(new Track("Research", "#5e81ac"));
        SessionHeader session = sessions.save(session("Liked session", true, research));
        User user = new User();
        user.setEmail("attendee@example.com");
        user.setPassword("test-password");
        user.setFirstname("Test");
        user.setLastname("Attendee");
        user.setRole(Role.USER);
        user.setLikedBy(new ArrayList<>(List.of(session)));
        users.saveAndFlush(user);

        sessions.deleteAllLikesForSession(session.getId());
        sessions.deleteById(session.getId());
        sessions.flush();

        assertEquals(false, sessions.existsById(session.getId()));
    }

    private SessionHeader session(String name, boolean published, Track track) {
        SessionHeader session = new SessionHeader();
        session.setName(name);
        session.setHost("Test host");
        session.setLocation("Test room");
        session.setStartTime(LocalDateTime.of(2026, 8, 3, 9, 0));
        session.setEndTime(LocalDateTime.of(2026, 8, 3, 10, 0));
        session.setTrack(track);
        session.setIsPublished(published);
        return session;
    }
}
