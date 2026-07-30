package icpmapp.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AgendaPushUnsubscribeRequest(
    @NotBlank @Size(max = 2048) String endpoint
) {
}
