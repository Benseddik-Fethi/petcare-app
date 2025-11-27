package fr.benseddik.backend.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Active les classes @ConfigurationProperties.
 */
@Configuration
@EnableConfigurationProperties({
        JwtProperties.class,
        SecurityProperties.class
})
public class PropertiesConfig {
}