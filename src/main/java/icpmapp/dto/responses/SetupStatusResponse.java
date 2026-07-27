package icpmapp.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetupStatusResponse {
    private boolean setupRequired;
    private boolean setupBlocked;
    private boolean configured;
}
