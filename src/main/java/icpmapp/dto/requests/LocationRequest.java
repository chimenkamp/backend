package icpmapp.dto.requests;

import lombok.Data;

@Data
public class LocationRequest {
    private String id;
    private String name;
    private Integer capacity;
}
