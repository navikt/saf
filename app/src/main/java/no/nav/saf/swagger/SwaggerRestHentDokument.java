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
		@ApiResponse(code = 200, message = "OK.", response = void.class, responseHeaders =  @ResponseHeader(name = "Content-type", description = "Dokumentets format, f.eks vil dokument av typen PDF gi Content-type \"application\\pdf\", XML gi Content type \"application\\xml\"", response = String.class)),
		@ApiResponse(code = 400, message = "Ugyldig parametere brukt som input, dette kan for eksempel bety at variantformatet brukt ikke finnes"),
		@ApiResponse(code = 401, message = "Kunne ikke autentisere på grunn av probleme med OIDC tokenet, dette kan bety feil format på tokenet, utgått token, eller at brukeren mangler tilgang"),
		@ApiResponse(code = 404, message = "Dokument eller journalpost ble ikke funnet for de gitte parameterene, årsaken kan være at dokumentet ikke eksisterer")}
)
public @interface SwaggerRestHentDokument {
	@AliasFor(
			annotation = RequestMapping.class
	)
	String value() default "";
}
