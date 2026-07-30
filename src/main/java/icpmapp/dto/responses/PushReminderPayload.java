package icpmapp.dto.responses;

public record PushReminderPayload(
    Long sessionId,
    String sessionName,
    String location,
    String startTime,
    int minutesBefore,
    String title,
    String body,
    String url,
    String tag
) {
}
