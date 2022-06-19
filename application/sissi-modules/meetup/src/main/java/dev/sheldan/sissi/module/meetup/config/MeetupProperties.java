package dev.sheldan.sissi.module.meetup.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:meetup.properties")
public class MeetupProperties {
}
