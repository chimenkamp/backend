package icpmapp.services;

import icpmapp.dto.SessionHeaderDTO;
import icpmapp.entities.SessionHeader;
import icpmapp.entities.Track;
import icpmapp.entities.User;
import icpmapp.repository.SessionContentRepository;
import icpmapp.repository.SessionHeaderRepository;
import icpmapp.repository.UserRepository;
import icpmapp.services.impl.AgendaServiceImpl;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AgendaServiceImplTest {

    @Test
    void exposesOnlyPublishedSessionsWithTrackMetadata() {
        SessionHeaderRepository sessions = mock(SessionHeaderRepository.class);
        AgendaServiceImpl service = new AgendaServiceImpl(
            sessions,
            mock(SessionContentRepository.class),
            mock(UserRepository.class)
        );
        SessionHeaderDTO published = new SessionHeaderDTO(
            1L,
            "Opening keynote",
            "Dr. Rivera",
            "Main Hall",
            LocalDateTime.of(2026, 8, 3, 9, 0),
            LocalDateTime.of(2026, 8, 3, 10, 0),
            null,
            4L,
            "keynotes",
            "Keynotes",
            "#932092"
        );
        when(sessions.findPublishedSessionsWithLikes()).thenReturn(List.of(published));

        List<SessionHeaderDTO> result = service.fetchAll();

        assertEquals(List.of(published), result);
        verify(sessions).findPublishedSessionsWithLikes();
    }

    @Test
    void includesTrackMetadataForPersonalAgendaSessions() {
        SessionHeaderRepository sessions = mock(SessionHeaderRepository.class);
        AgendaServiceImpl service = new AgendaServiceImpl(
            sessions,
            mock(SessionContentRepository.class),
            mock(UserRepository.class)
        );
        Track track = new Track("research", "Research", "#5e81ac");
        SessionHeader session = new SessionHeader();
        session.setId(7L);
        session.setName("Parallel research session");
        session.setStartTime(LocalDateTime.of(2026, 8, 4, 11, 0));
        session.setEndTime(LocalDateTime.of(2026, 8, 4, 12, 0));
        session.setTrack(track);
        session.setIsPublished(true);
        SessionHeader draft = new SessionHeader();
        draft.setId(8L);
        draft.setName("Unpublished parallel session");
        draft.setIsPublished(false);
        when(sessions.findByLikes_Id(3)).thenReturn(List.of(session, draft));

        List<SessionHeaderDTO> result = service.findLikedSessionsByUser(3);

        assertEquals(1, result.size());
        assertEquals("research", result.get(0).getTrackId());
        assertEquals("Research", result.get(0).getTrackName());
        assertEquals("#5e81ac", result.get(0).getTrackColor());
        assertTrue(result.stream().noneMatch(item -> item.getId().equals(8L)));
    }

    @Test
    void resolvesTheCurrentUsersFavoriteSessionsFromTheJwtUsername() {
        SessionHeaderRepository sessions = mock(SessionHeaderRepository.class);
        UserRepository users = mock(UserRepository.class);
        AgendaServiceImpl service = new AgendaServiceImpl(
            sessions,
            mock(SessionContentRepository.class),
            users
        );
        User currentUser = new User();
        currentUser.setId(9);
        currentUser.setEmail("attendee@example.com");
        when(users.findByEmail("attendee@example.com")).thenReturn(java.util.Optional.of(currentUser));
        when(sessions.findByLikes_Id(9)).thenReturn(List.of());

        List<SessionHeaderDTO> result = service.findLikedSessionsByUsername("attendee@example.com");

        assertTrue(result.isEmpty());
        verify(sessions).findByLikes_Id(9);
    }
}
