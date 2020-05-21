package jp.bragnikita.manan.backend.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;

public class SimpleUserDetailsService implements UserDetailsService {

    private final String username;
    private final String password;

    public SimpleUserDetailsService(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null) throw new UsernameNotFoundException("User is null");

        if (username.equals(this.username)) {
            return new User(username, this.password, new ArrayList<>());
        }
        throw new UsernameNotFoundException("Unknown user " + username);
    }
}
