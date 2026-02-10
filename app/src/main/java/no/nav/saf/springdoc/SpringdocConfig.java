package no.nav.saf.springdoc;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static io.swagger.v3.oas.models.security.SecurityScheme.In.HEADER;
import static io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@ConditionalOnProperty(
		value = {"springdoc.enabled"},
		havingValue = "true"
)
@Configuration
public class SpringdocConfig {

	@Bean
	public OpenAPI safRestApi(@Value("${NAIS_APP_IMAGE:1-SNAPSHOT}") String version) {
		return new OpenAPI()
				.info(new Info()
						.title("saf REST API")
						.description("""
								saf REST API tilbyr en tjeneste for å hente dokument fra fagarkivet.
								
								Klienter autoriseres med OAuth 2.0 access tokens. De kan være autorisert av Entra client credential flow eller Entra on-behalf-of flow.
								
								Azure access tokens for manuell test som saksbehandler kan hentes fra <a href="https://ida.intern.nav.no/">IDA</a>.
								""")
						.version(version))
				.components(
						new Components()
								.addSecuritySchemes("Authorization",
										new SecurityScheme()
												.type(HTTP)
												.scheme("Bearer")
												.bearerFormat("JWT")
												.in(HEADER)
												.description("Bearer token for autorisasjon. Må være issued av Entra client credential flow eller Entra on-behalf-of flow")
												.name(AUTHORIZATION)
								)
				)
				.addSecurityItem(
						new SecurityRequirement()
								.addList("Authorization")
				);
	}
}
