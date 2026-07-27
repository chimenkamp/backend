package icpmapp.services;

import icpmapp.dto.requests.BootstrapSetupRequest;
import icpmapp.dto.requests.JwtAuthenticationResponse;
import icpmapp.dto.responses.SetupStatusResponse;

public interface SetupService {
    SetupStatusResponse getStatus();
    JwtAuthenticationResponse bootstrap(BootstrapSetupRequest request);
}
