package no.nav.saf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation;


@Import(value = {ApplicationConfig.class})
@SpringBootApplication
@EnableJwtTokenValidation(ignore = {"org.springframework", "springfox.documentation"})
public class Application {
	public static void main(String[] args) {
		// Lettuce-spring boot interaksjon. Se https://github.com/lettuce-io/lettuce-core/issues/1767
		System.setProperty("io.lettuce.core.jfr", "false");
		SpringApplication.run(Application.class, args);
	}
}
