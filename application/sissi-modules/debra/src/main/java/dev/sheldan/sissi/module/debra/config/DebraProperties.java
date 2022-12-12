package dev.sheldan.sissi.module.debra.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
@ConfigurationProperties(prefix = "sissi.debra")
public class DebraProperties {
    private String websocketURL;
    private String donationAPIUrl;
}
