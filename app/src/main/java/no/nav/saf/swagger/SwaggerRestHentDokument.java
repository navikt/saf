package no.nav.saf.swagger;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.AliasFor;
import org.springframework.http.HttpHeaders;
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
		@ApiResponse(code = 200, message = "OK - dokument returneres.", response = byte[].class,
				responseHeaders = {
						@ResponseHeader(name = HttpHeaders.CONTENT_TYPE, description = "Mimetypen til dokumentet. Eksempel: Content-Type: application/pdf.", response = String.class),
						@ResponseHeader(name = HttpHeaders.CONTENT_DISPOSITION, description = "Hvordan dokumentet skal vises og filnavnet hvis det skal lastes ned. " +
								"Standardverdi er inline for visning. Filnavnet er formattert som <dokumentInfoId>_<variantformat>.<filendelse>. " +
								"Fileendelse vil være tilpasset for mimetypen, f.eks Content-Type: application/pdf vil gi filendelse .pdf. " +
								"Eksempel: Content-Disposition: inline; filename=400000000_ARKIV.pdf", response = String.class)
				}
		),
		@ApiResponse(code = 400, message = "* Ugyldig input. JournalpostId og dokumentInfoId må være tall og variantFormat må være en gyldig kodeverk-verdi.\n* Journalposten tilhører et ustøttet arkivsaksystem. Arkivsaksystem må være GSAK, PSAK eller NULL (midlertidig journalpost)."),
		@ApiResponse(code = 401, message = "* Vi vet ikke hvem bruker er og token kan ikke valideres.\n* F.eks ugyldig, utgått eller manglende OIDC token."),
		@ApiResponse(code = 403, message = "* Vi vet hvem bruker er og bruker får ikke tilgang.\n* F.eks dokumentet tilhører egen ansatt, har hemmelig adresse eller ikke har tilgang til tema og bruker har ikke tilgang til dette."),
		@ApiResponse(code = 404, message = "Dokument eller journalpost ble ikke funnet.")}
)
public @interface SwaggerRestHentDokument {
	@AliasFor(
			annotation = RequestMapping.class
	)
	String value() default "";
}
