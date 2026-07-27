package icpmapp.dto.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

@Data
public class BootstrapSetupRequest {

    @NotBlank
    @ToString.Exclude
    private String setupToken;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 12)
    @ToString.Exclude
    private String password;

    @NotBlank
    private String firstname;

    @NotBlank
    private String lastname;
}
