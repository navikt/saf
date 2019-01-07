package no.nav.saf.swagger;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@ConditionalOnProperty(
		value = {"swagger.enabled"},
		havingValue = "true"
)
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ApiResponses(value = {
		@ApiResponse(code = 200, message = "OK - dokument returneres.", response = void.class, responseHeaders =  @ResponseHeader(name = "Content-type", description = "Identifiserer filformatet til dokumentet. F.eks vil et dokument av typen PDF gi Content-type \"application\\pdf\", mens et dokument av typen XML gir Content type \"text\\xml\"", response = String.class)),
		@ApiResponse(code = 400, message = "* Ugyldig input. JournalpostId og dokumentId må være tall og variantFormat må være en gyldig kodeverk-verdi.\n* Journalposten tilhører et ustøttet arkivsaksystem. Arkivsaksystem må være GSAK, PSAK eller NULL (midlertidig journalpost)."),
		@ApiResponse(code = 401, message = "* Bruker mangler tilgang for å vise dokumentet.\n* Ugyldig OIDC token. Denne feilen gis dersom tokenet ikke har riktig format eller er utgått."),
		@ApiResponse(code = 404, message = "Dokument eller journalpost ble ikke funnet.")}
)
public @interface SwaggerRestHentDokument {
	@AliasFor(
			annotation = RequestMapping.class
	)
	String value() default "";
}
