package icpmapp.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaResponse {
    private UUID id;
    private String url;
    private String filename;
    private String originalFilename;
    private String mimeType;
    private Long fileSize;
    private String category;
    private String altText;
    private LocalDateTime createdAt;
}
