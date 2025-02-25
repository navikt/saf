package no.nav.saf;

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;


@Import(value = {ApplicationConfig.class})
@SpringBootApplication
@EnableAsync
@EnableJwtTokenValidation(ignore = {"org.springframework", "org.springdoc" })
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
