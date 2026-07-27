package com.example.RAG;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration for serving static assets.
 *
 * <p>Maps URL paths to resources located under {@code classpath:/templates/}
 * so the frontend can load CSS, JavaScript, and images from the relocated
 * templates directory.</p>
 *
 * <p>Without this configuration, Spring Boot would look for static assets
 * under {@code /static} or {@code /public} by default.</p>
 *
 * @see HomeController  serves the main HTML page
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Registers resource handlers for the frontend assets.
     *
     * <ul>
     *   <li>{@code /css/**} → {@code classpath:/templates/css/}</li>
     *   <li>{@code /js/**}  → {@code classpath:/templates/js/}</li>
     *   <li>{@code /me.png} → {@code classpath:/templates/}</li>
     * </ul>
     *
     * @param registry the resource handler registry provided by Spring MVC
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // CSS stylesheets
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/templates/css/");

        // JavaScript files
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/templates/js/");

        // Profile / avatar image
        registry.addResourceHandler("/me.png")
                .addResourceLocations("classpath:/templates/");
    }
}
