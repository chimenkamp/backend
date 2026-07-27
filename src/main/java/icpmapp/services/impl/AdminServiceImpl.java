package icpmapp.services.impl;

import icpmapp.dto.requests.*;
import icpmapp.dto.responses.*;
import icpmapp.entities.*;
import icpmapp.repository.*;
import icpmapp.services.AdminService;
import icpmapp.services.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final SessionHeaderRepository sessionHeaderRepository;
    private final SessionContentRepository sessionContentRepository;
    private final ContentRepository contentRepository;
    private final TrackRepository trackRepository;
    private final LocationRepository locationRepository;
    private final AdminMessageRepository adminMessageRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // ==================== Dashboard ====================

    @Override
    public DashboardStatsResponse getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalSessions = sessionHeaderRepository.count();
        long totalMessages = adminMessageRepository.count();
        long activeUsers = userRepository.findAll().stream()
                .filter(user -> user.getIsActive() != null && user.getIsActive())
                .count();

        return new DashboardStatsResponse(totalUsers, totalSessions, totalMessages, activeUsers);
    }

    // ==================== Content Management ====================

    @Override
    public List<ContentResponse> getAllContent(ContentCategory category, ContentType type, String search) {
        List<Content> contents;

        if (search != null && !search.isEmpty()) {
            if (category != null && type != null) {
                contents = contentRepository.searchByKeyOrValueAndCategoryAndType(search, category.name(), type.name());
            } else if (category != null) {
                contents = contentRepository.searchByKeyOrValueAndCategory(search, category.name());
            } else if (type != null) {
                contents = contentRepository.searchByKeyOrValueAndType(search, type.name());
            } else {
                contents = contentRepository.searchByKeyOrValue(search);
            }
        } else {
            if (category != null && type != null) {
                contents = contentRepository.findByCategoryAndType(category, type);
            } else if (category != null) {
                contents = contentRepository.findByCategory(category);
            } else if (type != null) {
                contents = contentRepository.findByType(type);
            } else {
                contents = contentRepository.findAll();
            }
        }

        return contents.stream()
                .map(this::mapToContentResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ContentResponse getContentById(String id) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Content not found"));
        return mapToContentResponse(content);
    }

    @Override
    @Transactional
    public ContentResponse createContent(ContentRequest request, String adminUserId) {
        Content content = new Content();
        content.setKey(request.getKey());
        content.setValue(request.getValue());
        content.setType(request.getType());
        content.setCategory(request.getCategory());
        content.setUpdatedBy(adminUserId);

        Content saved = contentRepository.save(content);
        return mapToContentResponse(saved);
    }

    @Override
    @Transactional
    public ContentResponse updateContent(String id, UpdateContentRequest request, String adminUserId) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Content not found"));

        if (request.getValue() != null) {
            content.setValue(request.getValue());
        }
        if (request.getType() != null) {
            content.setType(request.getType());
        }
        if (request.getCategory() != null) {
            content.setCategory(request.getCategory());
        }
        content.setUpdatedBy(adminUserId);

        Content saved = contentRepository.save(content);
        return mapToContentResponse(saved);
    }

    @Override
    @Transactional
    public void deleteContent(String id) {
        if (!contentRepository.existsById(id)) {
            throw new RuntimeException("Content not found");
        }
        contentRepository.deleteById(id);
    }

    private ContentResponse mapToContentResponse(Content content) {
        return new ContentResponse(
                content.getId(),
                content.getKey(),
                content.getValue(),
                content.getType(),
                content.getCategory(),
                content.getCreatedAt(),
                content.getUpdatedAt(),
                content.getUpdatedBy()
        );
    }

    // ==================== User Management ====================

    @Override
    public UserListResponse getUsers(int page, int limit, String search, String filter) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("id").descending());
        Page<User> userPage;

        if (search != null && !search.isEmpty()) {
            userPage = userRepository.searchWithConditionalPrivacy(search.toLowerCase(), pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        List<User> filteredUsers = userPage.getContent();

        if (filter != null && !filter.isEmpty()) {
            filteredUsers = filteredUsers.stream()
                    .filter(user -> {
                        switch (filter.toLowerCase()) {
                            case "active":
                                return user.getIsActive() != null && user.getIsActive();
                            case "inactive":
                                return user.getIsActive() == null || !user.getIsActive();
                            case "admin":
                                return user.getRole() == Role.ADMIN;
                            default:
                                return true;
                        }
                    })
                    .collect(Collectors.toList());
        }

        List<AdminUserResponse> userResponses = filteredUsers.stream()
                .map(this::mapToAdminUserResponse)
                .collect(Collectors.toList());

        return new UserListResponse(
                userResponses,
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                page
        );
    }

    @Override
    public AdminUserResponse getUserById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToAdminUserResponse(user);
    }

    @Override
    @Transactional
    public AdminUserResponse createUser(CreateUserRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        user.setCompany(request.getCompany());
        user.setCountry(request.getCountry());
        user.setSharingchoice(request.getShareInfo());
        user.setIsActive(request.getIsActive());
        user.setRole(Boolean.TRUE.equals(request.getIsAdmin()) ? Role.ADMIN : Role.USER);

        User saved = userRepository.save(user);
        return mapToAdminUserResponse(saved);
    }

    @Override
    @Transactional
    public AdminUserResponse updateUser(Integer id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getFirstname() != null) {
            user.setFirstname(request.getFirstname());
        }
        if (request.getLastname() != null) {
            user.setLastname(request.getLastname());
        }
        if (request.getCompany() != null) {
            user.setCompany(request.getCompany());
        }
        if (request.getCountry() != null) {
            user.setCountry(request.getCountry());
        }
        if (request.getShareInfo() != null) {
            user.setSharingchoice(request.getShareInfo());
        }
        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }
        if (request.getIsAdmin() != null) {
            user.setRole(Boolean.TRUE.equals(request.getIsAdmin()) ? Role.ADMIN : Role.USER);
        }

        User saved = userRepository.save(user);
        return mapToAdminUserResponse(saved);
    }

    @Override
    @Transactional
    public void deleteUser(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public PasswordResetResponse resetUserPassword(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate a temporary password or reset token
        String tempPassword = UUID.randomUUID().toString().substring(0, 8);
        user.setPassword(passwordEncoder.encode(tempPassword));
        userRepository.save(user);

        // Send email with reset instructions (you might want to implement a proper reset token flow)
        try {
            emailService.sendEmail(user.getEmail(), "Password Reset",
                    "Your temporary password is: " + tempPassword + ". Please change it after logging in.");
        } catch (Exception e) {
            // Log error but don't fail the operation
        }

        return new PasswordResetResponse("Password reset email sent", user.getEmail());
    }

    private AdminUserResponse mapToAdminUserResponse(User user) {
        String avatarUrl = user.getAvatar_path() != null ?
                "/api/v1/account/avatar/" + user.getId() : null;

        return new AdminUserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstname(),
                user.getLastname(),
                user.getCompany(),
                user.getCountry(),
                avatarUrl,
                user.getIsActive(),
                user.getRole() == Role.ADMIN,
                user.getSharingchoice(),
                user.getCreatedAt(),
                user.getLastLogin()
        );
    }

    // ==================== Messaging ====================

    @Override
    public AdminMessageListResponse getMessages(int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<AdminMessage> messagePage = adminMessageRepository.findAllByOrderBySentAtDesc(pageable);

        List<AdminMessageResponse> messageResponses = messagePage.getContent().stream()
                .map(this::mapToAdminMessageResponse)
                .collect(Collectors.toList());

        return new AdminMessageListResponse(
                messageResponses,
                messagePage.hasNext(),
                messagePage.getTotalElements()
        );
    }

    @Override
    public AdminMessageResponse getMessageById(String id) {
        AdminMessage message = adminMessageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        return mapToAdminMessageResponse(message);
    }

    @Override
    @Transactional
    public AdminMessageSentResponse sendMessage(AdminMessageRequest request, String adminUserId) {
        List<User> recipients = new ArrayList<>();

        switch (request.getRecipientType()) {
            case ALL:
                recipients = userRepository.findAll();
                break;
            case ACTIVE:
                recipients = userRepository.findAll().stream()
                        .filter(user -> user.getIsActive() != null && user.getIsActive())
                        .collect(Collectors.toList());
                break;
            case SELECTED:
                if (request.getRecipients() == null || request.getRecipients().isEmpty()) {
                    throw new RuntimeException("No recipients selected");
                }
                for (Integer userId : request.getRecipients()) {
                    userRepository.findById(userId).ifPresent(recipients::add);
                }
                break;
        }

        if (recipients.isEmpty()) {
            throw new RuntimeException("No recipients selected");
        }

        AdminMessage message = new AdminMessage();
        message.setTitle(request.getTitle());
        message.setMessage(request.getMessage());
        message.setPriority(request.getPriority() != null ? request.getPriority() : MessagePriority.NORMAL);
        message.setRecipientType(request.getRecipientType());
        message.setRecipients(recipients);
        message.setRecipientCount(recipients.size());
        message.setPushSent(request.getSendPush());
        message.setEmailSent(request.getSendEmail());
        message.setSentBy(adminUserId);
        message.setReadBy(new ArrayList<>());

        AdminMessage saved = adminMessageRepository.save(message);

        // Send push notifications if requested
        if (Boolean.TRUE.equals(request.getSendPush())) {
            // TODO: Implement push notification logic
        }

        // Send emails if requested
        if (Boolean.TRUE.equals(request.getSendEmail())) {
            for (User recipient : recipients) {
                try {
                    emailService.sendEmail(recipient.getEmail(), request.getTitle(), request.getMessage());
                } catch (Exception e) {
                    // Log error but continue
                }
            }
        }

        return new AdminMessageSentResponse(
                saved.getId(),
                saved.getTitle(),
                saved.getRecipientCount(),
                saved.getPushSent(),
                saved.getEmailSent(),
                saved.getSentAt()
        );
    }

    private AdminMessageResponse mapToAdminMessageResponse(AdminMessage message) {
        List<Integer> recipientIds = message.getRecipients() != null ?
                message.getRecipients().stream().map(User::getId).collect(Collectors.toList()) :
                new ArrayList<>();

        return new AdminMessageResponse(
                message.getId(),
                message.getTitle(),
                message.getMessage(),
                message.getPriority(),
                message.getRecipientType(),
                recipientIds,
                message.getRecipientCount(),
                message.getReadBy() != null ? message.getReadBy().size() : 0,
                message.getPushSent(),
                message.getEmailSent(),
                message.getSentAt(),
                message.getSentBy()
        );
    }

    // ==================== Schedule - Sessions ====================

    @Override
    public List<SessionResponse> getSessions(LocalDateTime date, String trackId, Boolean published) {
        List<SessionHeader> sessions = sessionHeaderRepository.findAll();

        return sessions.stream()
                .filter(session -> {
                    boolean matches = true;
                    if (date != null) {
                        matches = session.getStartTime() != null &&
                                session.getStartTime().toLocalDate().equals(date.toLocalDate());
                    }
                    if (trackId != null && session.getTrack() != null) {
                        matches = matches && trackId.equals(session.getTrack().getId());
                    }
                    if (published != null) {
                        matches = matches && published.equals(session.getIsPublished());
                    }
                    return matches;
                })
                .map(this::mapToSessionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SessionResponse getSessionById(Long id) {
        SessionHeader session = sessionHeaderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        return mapToSessionResponse(session);
    }

    @Override
    @Transactional
    public SessionResponse createSession(SessionRequest request) {
        SessionHeader session = new SessionHeader();
        session.setName(request.getName());
        session.setHost(request.getHost());
        session.setLocation(request.getLocation());
        session.setStartTime(request.getStartTime());
        session.setEndTime(request.getEndTime());
        session.setIsPublished(request.getIsPublished());

        if (request.getTrack() != null) {
            Track track = trackRepository.findById(request.getTrack()).orElse(null);
            session.setTrack(track);
        }

        SessionHeader saved = sessionHeaderRepository.save(session);

        // Create session content if provided
        if (request.getContent() != null) {
            SessionContent content = new SessionContent();
            content.setContent(request.getContent());
            content.setHeader(saved);
            sessionContentRepository.save(content);
            saved.setContent(content);
        }

        return mapToSessionResponse(saved);
    }

    @Override
    @Transactional
    public SessionResponse updateSession(Long id, SessionRequest request) {
        SessionHeader session = sessionHeaderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (request.getName() != null) {
            session.setName(request.getName());
        }
        if (request.getHost() != null) {
            session.setHost(request.getHost());
        }
        if (request.getLocation() != null) {
            session.setLocation(request.getLocation());
        }
        if (request.getStartTime() != null) {
            session.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            session.setEndTime(request.getEndTime());
        }
        if (request.getIsPublished() != null) {
            session.setIsPublished(request.getIsPublished());
        }
        if (request.getTrack() != null) {
            Track track = trackRepository.findById(request.getTrack()).orElse(null);
            session.setTrack(track);
        }

        // Update content
        if (request.getContent() != null) {
            if (session.getContent() != null) {
                session.getContent().setContent(request.getContent());
                sessionContentRepository.save(session.getContent());
            } else {
                SessionContent content = new SessionContent();
                content.setContent(request.getContent());
                content.setHeader(session);
                sessionContentRepository.save(content);
                session.setContent(content);
            }
        }

        SessionHeader saved = sessionHeaderRepository.save(session);
        return mapToSessionResponse(saved);
    }

    @Override
    @Transactional
    public void deleteSession(Long id) {
        if (!sessionHeaderRepository.existsById(id)) {
            throw new RuntimeException("Session not found");
        }
        sessionHeaderRepository.deleteById(id);
    }

    private SessionResponse mapToSessionResponse(SessionHeader session) {
        String trackColor = session.getTrack() != null ? session.getTrack().getColor() : null;
        String trackId = session.getTrack() != null ? session.getTrack().getId() : null;
        String contentText = session.getContent() != null ? session.getContent().getContent() : null;

        return new SessionResponse(
                session.getId(),
                session.getName(),
                session.getHost(),
                session.getLocation(),
                session.getStartTime(),
                session.getEndTime(),
                contentText,
                trackId,
                trackColor,
                session.getIsPublished()
        );
    }

    // ==================== Schedule - Tracks ====================

    @Override
    public List<TrackResponse> getTracks() {
        return trackRepository.findAll().stream()
                .map(track -> new TrackResponse(track.getId(), track.getName(), track.getColor()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<TrackResponse> updateTracks(List<TrackRequest> trackRequests) {
        List<Track> updatedTracks = new ArrayList<>();

        for (TrackRequest request : trackRequests) {
            Track track;
            if (request.getId() != null && trackRepository.existsById(request.getId())) {
                track = trackRepository.findById(request.getId()).get();
                track.setName(request.getName());
                track.setColor(request.getColor());
            } else {
                track = new Track(request.getName(), request.getColor());
            }
            updatedTracks.add(trackRepository.save(track));
        }

        return updatedTracks.stream()
                .map(track -> new TrackResponse(track.getId(), track.getName(), track.getColor()))
                .collect(Collectors.toList());
    }

    // ==================== Schedule - Locations ====================

    @Override
    public List<LocationResponse> getLocations() {
        return locationRepository.findAll().stream()
                .map(loc -> new LocationResponse(loc.getId(), loc.getName(), loc.getCapacity()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<LocationResponse> updateLocations(List<LocationRequest> locationRequests) {
        List<Location> updatedLocations = new ArrayList<>();

        for (LocationRequest request : locationRequests) {
            Location location;
            if (request.getId() != null && locationRepository.existsById(request.getId())) {
                location = locationRepository.findById(request.getId()).get();
                location.setName(request.getName());
                location.setCapacity(request.getCapacity());
            } else {
                location = new Location(request.getName(), request.getCapacity());
            }
            updatedLocations.add(locationRepository.save(location));
        }

        return updatedLocations.stream()
                .map(loc -> new LocationResponse(loc.getId(), loc.getName(), loc.getCapacity()))
                .collect(Collectors.toList());
    }
}
