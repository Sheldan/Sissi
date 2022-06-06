package dev.sheldan.sissi.module.quotes.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:quotes.properties")
public class QuotesProperties {
}
