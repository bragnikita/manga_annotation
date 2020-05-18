package jp.bragnikita.manan.backend.config;

import jp.bragnikita.manan.backend.controllers.utils.PathWildcardParameterResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebServiceConfig {

    @Value("${app.timeline.storage.root}")
    private String storageLocation;

    @Value("${app.website.root}")
    private String siteLocation;

    @Bean
    public WebMvcConfigurer applicationWebMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**").allowedOrigins("*").allowedHeaders("*").allowedMethods("*");
            }

            @Override
            public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
                resolvers.add(new PathWildcardParameterResolver());
            }

            @Override
            public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {

            }

            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {


                String attachmentsDir;
                if (Path.of(storageLocation).isAbsolute()) {
                    attachmentsDir = storageLocation;
                } else {
                    attachmentsDir = Paths.get(storageLocation).toAbsolutePath().toString();
                }
                if (!attachmentsDir.endsWith("/"))
                    attachmentsDir = attachmentsDir + "/";
                registry.addResourceHandler("/static/**")
                        .addResourceLocations("file:" + attachmentsDir)
                        .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS));

                Path staticContentDir = Paths.get(siteLocation).toAbsolutePath();
                System.out.println(staticContentDir.toString());
                if (staticContentDir.toFile().exists()) {
                    System.out.println("exists");
                    registry.addResourceHandler("/", "/**")
                            .addResourceLocations("file:" + staticContentDir.toString() + "/");
                }
            }
        };
    }

}
