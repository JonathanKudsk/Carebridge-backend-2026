package com.carebridge.dtos;

import com.carebridge.entities.User;
import com.carebridge.entities.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserWithLocationDTO {
    private Long id;
    private String name;
    private String email;
    private Role role;

    private String displayName;
    private String displayEmail;
    private String displayPhone;
    private String internalEmail;
    private String internalPhone;
    private boolean isEmployed;

    private LocationResponseDTO[] locations;

    public UserWithLocationDTO(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.displayName = user.getDisplayName();
        this.displayEmail = user.getDisplayEmail();
        this.displayPhone = user.getDisplayPhone();
        this.internalEmail = user.getInternalEmail();
        this.internalPhone = user.getInternalPhone();
        this.isEmployed = user.isEmployed();
        this.locations = user.getLocations().stream().map(LocationResponseDTO::new).toArray(LocationResponseDTO[]::new);
    }
}