package icpmapp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "conferia.reminders")
public class AgendaReminderProperties {
    private String publicKey = "";
    private String privateKey = "";
    private String subject = "";
    private int minutesBefore = 15;

    public boolean isConfigured() {
        return !publicKey.isBlank() && !privateKey.isBlank() && !subject.isBlank();
    }
}
