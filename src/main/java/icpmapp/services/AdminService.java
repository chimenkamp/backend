package icpmapp.services;

import icpmapp.dto.requests.*;
import icpmapp.dto.responses.*;
import icpmapp.entities.ContentCategory;
import icpmapp.entities.ContentType;

import java.time.LocalDateTime;
import java.util.List;

public interface AdminService {

    // Dashboard
    DashboardStatsResponse getDashboardStats();

    // Content Management
    List<ContentResponse> getAllContent(ContentCategory category, ContentType type, String search);
    ContentResponse getContentById(String id);
    ContentResponse createContent(ContentRequest request, String adminUserId);
    ContentResponse updateContent(String id, UpdateContentRequest request, String adminUserId);
    void deleteContent(String id);

    // User Management
    UserListResponse getUsers(int page, int limit, String search, String filter);
    AdminUserResponse getUserById(Integer id);
    AdminUserResponse createUser(CreateUserRequest request);
    AdminUserResponse updateUser(Integer id, UpdateUserRequest request);
    void deleteUser(Integer id);
    PasswordResetResponse resetUserPassword(Integer id);

    // Messaging
    AdminMessageListResponse getMessages(int page, int limit);
    AdminMessageResponse getMessageById(String id);
    AdminMessageSentResponse sendMessage(AdminMessageRequest request, String adminUserId);

    // Schedule - Sessions
    List<SessionResponse> getSessions(LocalDateTime date, String trackId, Boolean published);
    SessionResponse getSessionById(Long id);
    SessionResponse createSession(SessionRequest request);
    SessionResponse updateSession(Long id, SessionRequest request);
    void deleteSession(Long id);

    // Schedule - Tracks
    List<TrackResponse> getTracks();
    List<TrackResponse> updateTracks(List<TrackRequest> tracks);

    // Schedule - Locations
    List<LocationResponse> getLocations();
    List<LocationResponse> updateLocations(List<LocationRequest> locations);
}
