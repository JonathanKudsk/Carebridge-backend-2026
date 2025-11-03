package com.carebridge.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtUserDTO {
    private String username;
    @JsonIgnore
    private String password;
    @Builder.Default
    private Set<String> roles = new HashSet<>();
}
