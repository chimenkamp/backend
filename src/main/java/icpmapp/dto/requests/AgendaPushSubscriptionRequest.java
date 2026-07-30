package icpmapp.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AgendaPushSubscriptionRequest(
    @NotBlank @Size(max = 2048) String endpoint,
    @NotBlank @Size(max = 255) String p256dh,
    @NotBlank @Size(max = 255) String auth,
    @Size(max = 16) String locale
) {
}
