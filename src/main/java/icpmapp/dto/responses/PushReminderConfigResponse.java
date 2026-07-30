package icpmapp.dto.responses;

public record PushReminderConfigResponse(
    boolean available,
    String publicKey,
    int minutesBefore
) {
}
