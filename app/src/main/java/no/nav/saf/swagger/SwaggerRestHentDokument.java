package no.nav.saf.swagger;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.Example;
import io.swagger.annotations.ExampleProperty;
import io.swagger.annotations.ResponseHeader;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

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
@ApiOperation(value = "Henter fysiske dokumenter fra NAV sitt arkiv og gjør nødvendig tilgangskontroll.", authorizations = {@Authorization(value = "apiKey")})
@ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK - dokument returneres. Representasjonen er en base64 encoded string.", response = byte[].class,
				examples = @Example(value = {@ExampleProperty(mediaType = MediaType.APPLICATION_PDF_VALUE, value = "JVBERi0xLjcgQmFzZTY0IGVuY29kZXQgZnlzaXNrIGRva3VtZW50")}),
                responseHeaders = {
                        @ResponseHeader(name = HttpHeaders.CONTENT_TYPE, description = "Mimetypen til dokumentet. Eksempel: Content-Type: application/pdf.", response = String.class),
                        @ResponseHeader(name = HttpHeaders.CONTENT_DISPOSITION, description = "Hvordan dokumentet skal vises og filnavnet hvis det skal lastes ned. " +
                                "Standardverdi er inline for visning. Filnavnet er formattert som <dokumentInfoId>_<variantformat>.<filendelse>. " +
                                "Fileendelse vil være tilpasset for mimetypen, f.eks Content-Type: application/pdf vil gi filendelse .pdf. " +
                                "Eksempel: Content-Disposition: inline; filename=400000000_ARKIV.pdf", response = String.class)
                }
        ),
        @ApiResponse(code = 400, message = "* Ugyldig input. JournalpostId og dokumentInfoId må være tall og variantFormat må være en gyldig kodeverk-verdi som ARKIV eller ORIGINAL.\n* Journalposten tilhører et ustøttet arkivsaksystem. Arkivsaksystem må være GSAK, PSAK eller NULL (midlertidig journalpost)."),
        @ApiResponse(code = 401, message = "* Vi kan ikke autorisere bruker gjennom token eller system som har gitt token er ukjent for saf.\n* F.eks ugyldig, utgått, manglende OIDC token eller ingen audience hos saf."),
        @ApiResponse(code = 403, message = "* Vi kan ikke gi tilgang til dokumentet på grunn av sikkerhet eller personvern.\n* F.eks dokumentet tilhører egen ansatt eller bruker som bor på hemmelig adresse. Eller bruker har ikke tilgang til tema."),
        @ApiResponse(code = 404, message = "* Dokumentet ble ikke funnet i fagarkivet.\n* Dette kan være av midlertidig natur i tilfeller der konsument får en claim check på en journalpostId før den er ferdig arkivert.\n* Det er opp til utvikleren å vurdere om det skal forsøkes retry på denne feilstatusen.")}
)
public @interface SwaggerRestHentDokument {
    String value() default "";
}
