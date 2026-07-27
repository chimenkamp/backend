package icpmapp.dto.responses;

import org.springframework.core.io.Resource;

public record MediaFileResponse(Resource resource, String mimeType) {
}
