package com.minimall.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000", "http://localhost:5173", "https://claude.ai")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/{path:^(?!api$|swagger-ui$|v3$|actuator$|error$|favicon\\.ico$)[^\\.]*$}")
                .setViewName("forward:/index.html");
        registry.addViewController("/{path:^(?!api$|swagger-ui$|v3$|actuator$|error$|favicon\\.ico$)[^\\.]*$}/**")
                .setViewName("forward:/index.html");
    }
}
