package ru.test.bankingapi.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "token")
public class TokenProperties {
    private String issuer = "banking-api";
    private String audience = "banking-api-clients";
    private Access access = new Access();
    private Refresh refresh = new Refresh();

    @Getter
    @Setter
    public static class Access {
        private String signingKey;
        private Duration expiration = Duration.ofMinutes(15);
    }

    @Getter
    @Setter
    public static class Refresh {
        private String signingKey;
        private Duration expiration = Duration.ofDays(7);
    }
}
