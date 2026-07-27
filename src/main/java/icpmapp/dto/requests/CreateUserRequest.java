package icpmapp.dto.requests;

import lombok.Data;

@Data
public class CreateUserRequest {
    private String email;
    private String password;
    private String firstname;
    private String lastname;
    private String company;
    private String country;
    private Boolean isActive = true;
    private Boolean isAdmin = false;
    private Boolean shareInfo = true;
}
