package jp.bragnikita.manan.backend.security;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class AuthencticationRequest {
    private String username;
    private String password;
}
