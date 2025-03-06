package ru.dankon.userinfo.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.admin")
public record AdminProperties(
        String username,
        String password,
        String role
) {}
