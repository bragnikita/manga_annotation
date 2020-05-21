package jp.bragnikita.manan.backend.security;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
@Lazy
public class FileUserDetailsService implements UserDetailsService {

    private Path pathToUsersFile;

    private Map<String, UsersRecord> userToPassword = new HashMap<>();

    public FileUserDetailsService(@Value("${app.security.usersfile}") String pathToUsersFile) {
        this.pathToUsersFile = Path.of(pathToUsersFile);
    }

    @PostConstruct
    @Scheduled(fixedRate = 1000 * 60)
    public void reload() {
        File file = pathToUsersFile.toFile();
        if (file.exists()) {
            try {
                ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                UsersFile f = mapper.readValue(file, UsersFile.class);
                synchronized (this) {
                    userToPassword.clear();
                    Arrays.stream(f.users).forEach((usersRecord -> {
                        userToPassword.put(usersRecord.username, usersRecord);
                    }));
                }
            } catch (IOException e) {
                System.err.println("Could not reload users file: " + e.getMessage());
            }
        } else {
            System.err.println("Users file not found " + file.getAbsolutePath());
        }
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null) throw new UsernameNotFoundException("User is null");
        UsersRecord usersRecord;
        synchronized (this) {
            usersRecord = this.userToPassword.get(username);
        }
        if (usersRecord == null) {
            throw new UsernameNotFoundException("User is not found");
        }
        return new User(username, usersRecord.password, new ArrayList<>());
    }

    @Getter
    @Setter
    @NoArgsConstructor
    private static class UsersFile {
        private UsersRecord[] users = new UsersRecord[]{};
    }

    @Getter
    @Setter
    @NoArgsConstructor
    private static class UsersRecord {
        private String username;
        private String password;
        private String[] roles = new String[]{};
    }
}
