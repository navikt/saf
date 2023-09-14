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
								Her dokumenteres REST tjenestegrensesnittet til sak- og arkivfasade (SAF).
								
								Til autentisering brukes OIDC-token (JWT via OAuth 2.0). Følgende format må brukes i Authorize sitt input-felt "Value": <strong>Bearer {token}</strong>.
								Eksempel på verdi i input-feltet: <strong> Bearer eYdmifml0ejugm</strong>. Et gyldig token kommer til å ha mange flere karakterer enn i eksempelet.
								
								Tokens for manuell test kan hentes fra <a href="https://ida.intern.nav.no/">IDA</a>. For maskinell test og produksjon kan tokens komme fra Azure V2 eller NAV REST-STS.
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
												.description("Eksempel på verdi som skal inn i Value-feltet (Bearer trengs altså ikke å oppgis): 'eyAidH...'")
												.name(AUTHORIZATION)
								)
				)
				.addSecurityItem(
						new SecurityRequirement()
								.addList("Authorization")
				);
	}
}
