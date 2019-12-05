package no.nav.saf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation;


@Import(value = {ApplicationConfig.class})
@SpringBootApplication
@EnableJwtTokenValidation(ignore={"org.springframework", "springfox.documentation"})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
