package icpmapp.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserResponse {
    private Integer id;
    private String email;
    private String firstname;
    private String lastname;
    private String company;
    private String country;
    private String avatar;
    private Boolean isActive;
    private Boolean isAdmin;
    private Boolean shareInfo;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
}
