package icpmapp.dto.requests;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String firstname;
    private String lastname;
    private String company;
    private String country;
    private Boolean isActive;
    private Boolean isAdmin;
    private Boolean shareInfo;
}
