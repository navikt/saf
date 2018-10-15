package no.nav.saf.restconfig;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Import({RestTemplateConfig.class})
@Configuration
public class RestConsumerConfig {

}
