package jp.bragnikita.manan.backend.config;

import jp.bragnikita.manan.backend.security.FileUserDetailsService;
import jp.bragnikita.manan.backend.security.JWTAuthenticationFilter;
import jp.bragnikita.manan.backend.security.JWTAuthorizatonFilter;
import jp.bragnikita.manan.backend.security.SimpleUserDetailsService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;

@Configuration
@EnableWebSecurity
@EnableScheduling
public class WebSecurityConfigDev extends WebSecurityConfigurerAdapter {

    @Autowired
    private Environment env;

    @Autowired
    private ApplicationContext context;


    @Override
    protected void configure(HttpSecurity http) throws Exception {
//        if (env.acceptsProfiles(Profiles.of("development | test"))) {
//            http.csrf().disable().authorizeRequests().anyRequest().permitAll();
//        } else {
//            http.csrf().disable().authorizeRequests().anyRequest().permitAll();
//        }
        String secret = env.getProperty("spring.security.jwt.secret");
        http.cors().and().csrf().disable().authorizeRequests()
                .antMatchers(HttpMethod.GET, "/**","/api/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilter(new JWTAuthenticationFilter(authenticationManager(), secret))
                .addFilter(new JWTAuthorizatonFilter(authenticationManager(), secret))
                .exceptionHandling()
                .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                .accessDeniedHandler(new JwtAuthenticationAccessDenied())
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService()).passwordEncoder(NoOpPasswordEncoder.getInstance());
    }


    public UserDetailsService userDetailsService() {
        if (env.acceptsProfiles(Profiles.of("test"))) {
            return new SimpleUserDetailsService("admin", "admin123");
        } else {
            System.out.println("FileUserDetailsService");
            return context.getBean(FileUserDetailsService.class);
        }
    }

    private static class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

        @Override
        public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON.getType());
            IOUtils.copy(new ByteArrayInputStream("{ message: \"Unauthorized\"}".getBytes()), response.getOutputStream());
        }
    }

    private static class JwtAuthenticationAccessDenied implements AccessDeniedHandler {

        @Override
        public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON.getType());
            IOUtils.copy(new ByteArrayInputStream("{ message: \"Access Denied\"}".getBytes()), response.getOutputStream());
        }
    }
}
