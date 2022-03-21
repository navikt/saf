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
import org.springframework.http.HttpHeaders;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
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
								Her dokumenteres REST tjenestegrensesnittet til sak og arkivfasade (SAF). <br/><br/>
								Til autentisering brukes OIDC-token (JWT via OAuth2.0). Følgende format må brukes i Authorize sitt input-felt "Value": <strong> Bearer {token} </strong>.\s
								Eksempel på verdi i input-feltet: <strong> Bearer eYdmifml0ejugm </strong>. Et gyldig token kommer til å ha mange flere karakterer enn i eksempelet.<br/><br/>
								Tokens for manuell test kan hentes fra <a href="https://ida.adeo.no/">IDA</a>. For maskinell test og produksjon kan tokens komme fra Azure V2, NAV REST-STS eller OpenAM.
								""")
						.version(version))
				.components(
						new Components()
								.addSecuritySchemes("Authorization",
										new SecurityScheme()
												.type(SecurityScheme.Type.HTTP)
												.scheme("Bearer")
												.bearerFormat("JWT")
												.in(SecurityScheme.In.HEADER)
												.description("Eksempel på verdi som skal inn i Value-feltet (Bearer trengs altså ikke å oppgis): 'eyAidH...'")
												.name(HttpHeaders.AUTHORIZATION)
								)
				)
				.addSecurityItem(
						new SecurityRequirement()
								.addList("Authorization")
				);
	}
}
