package icpmapp.controller;

import icpmapp.dto.requests.BootstrapSetupRequest;
import icpmapp.dto.requests.JwtAuthenticationResponse;
import icpmapp.dto.responses.SetupStatusResponse;
import icpmapp.services.SetupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/setup")
@RequiredArgsConstructor
public class SetupController {

    private final SetupService setupService;

    @GetMapping("/status")
    public ResponseEntity<SetupStatusResponse> getStatus() {
        return ResponseEntity.ok(setupService.getStatus());
    }

    @PostMapping("/bootstrap")
    public ResponseEntity<JwtAuthenticationResponse> bootstrap(
        @Valid @RequestBody BootstrapSetupRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(setupService.bootstrap(request));
    }
}
