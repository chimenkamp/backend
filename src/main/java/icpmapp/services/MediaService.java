package icpmapp.services;

import icpmapp.dto.responses.MediaResponse;
import icpmapp.dto.responses.MediaFileResponse;
import icpmapp.entities.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface MediaService {
    
    MediaResponse uploadMedia(MultipartFile file, String category, String altText, User uploadedBy);
    List<MediaResponse> getAllMedia();
    List<MediaResponse> getMediaByCategory(String category);
    MediaFileResponse getMediaFile(String filename);
    void deleteMedia(UUID id);
}
