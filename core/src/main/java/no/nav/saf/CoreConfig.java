package no.nav.saf;

import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@ComponentScan
@Configuration
public class CoreConfig {
	// Raskere JSON serialisering og deserialisering.
	// Se https://github.com/FasterXML/jackson-docs/wiki/Presentation:-Jackson-Performance
	@Bean
	AfterburnerModule afterburnerModule() {
		return new AfterburnerModule();
	}
}
