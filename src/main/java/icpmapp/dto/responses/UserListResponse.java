package icpmapp.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserListResponse {
    private List<AdminUserResponse> users;
    private Long totalUsers;
    private Integer totalPages;
    private Integer currentPage;
}
