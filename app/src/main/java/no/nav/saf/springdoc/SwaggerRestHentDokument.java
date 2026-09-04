package no.nav.saf.springdoc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

@ConditionalOnProperty(
		value = {"springdoc.enabled"},
		havingValue = "true"
)
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Operation(
		summary = "Hent et fysisk dokument fra NAVs arkiv",
		description = """
				Henter et fysisk dokument og utfører nødvendig tilgangskontroll.

				Dokumentet kan hentes med eller uten `variantFormat` i URL-en. Dersom `variantFormat` ikke oppgis
				returneres `SLADDET` dersom varianten finnes, ellers returneres `ARKIV`.
				""",
		operationId = "hentDokument"
)
@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Dokumentet returneres som rå data (binærdata hvis binært filformat som pdf, tekstdata hvis tekstformat som xml eller json).",
				headers = {
						@Header(name = CONTENT_TYPE, description = "Mimetypen til dokumentet. Eksempel: `Content-Type: application/pdf`.", required = true),
						@Header(name = CONTENT_DISPOSITION, description = """
								Hvordan dokumentet skal vises og filnavnet hvis det skal lastes ned.
								Standardverdi er inline for visning. Filnavnet er formattert som `<dokumentInfoId>_<variantformat>.<filendelse>`.
								Variantformatet er enten eksplisitt angitt av konsumenten eller automatisk valgt av SAF.
								Filendelsen tilpasses mimetypen, for eksempel gir `Content-Type: application/pdf` filendelsen `.pdf`.
								Eksempel: `Content-Disposition: inline; filename=400000000_ARKIV.pdf`.
								""", required = true)
				},
				content = {
						@Content(mediaType = APPLICATION_PDF_VALUE,
								examples = @ExampleObject(value = """
										%PDF-1.5
										%âãÏÓ
										3 0 obj
										<<
										/Linearized 1
										/L 18357
										/H [ 937 153 ]
										/O 5
										/E 17776
										/N 1
										/T 18171
										>>
										endobj
										
										... binærdata fortsetter
										""")),
						@Content(
								mediaType = APPLICATION_XML_VALUE,
								schema = @Schema(implementation = XmlExampleResponse.class),
								examples = @ExampleObject(
										value = """
												<skanningmetadata>
													<referansenummer>12345678</referansenummer>
												</skanningmetadata>
												"""
								)),
						@Content(mediaType = APPLICATION_JSON_VALUE,
								examples = @ExampleObject(value = """
										{
										    "skanningmetadata": {
										        "referansenummer": "12345678"
										    }
										}
										"""))
				}
		),
		@ApiResponse(responseCode = "400", description = """
				* Ugyldig input. `journalpostId` og `dokumentInfoId` må være tall.
				* Dersom `variantFormat` oppgis må det være en gyldig kodeverkverdi, for eksempel `ARKIV`, `SLADDET` eller `ORIGINAL`.
				* Journalposten tilhører et ustøttet arkivsaksystem. Arkivsaksystem må være GSAK, PSAK eller NULL (midlertidig journalpost).""",
				content = @Content(mediaType = APPLICATION_JSON_VALUE)),
		@ApiResponse(responseCode = "401", description = """
				* Vi kan ikke autorisere bruker gjennom token eller system som har gitt token er ukjent for saf.
				* F.eks ugyldig, utgått, manglende OIDC token eller ingen audience hos saf.""",
				content = @Content(mediaType = APPLICATION_JSON_VALUE)),
		@ApiResponse(responseCode = "403", description = """
				* Vi kan ikke gi tilgang til dokumentet på grunn av sikkerhet eller personvern.
				* F.eks dokumentet tilhører egen ansatt eller bruker som bor på hemmelig adresse. Eller bruker har ikke tilgang til tema.
				* Referer til [dokumentasjon om tilgangskontrollen til saf](https://confluence.adeo.no/display/BOA/saf+-+Tilgangskontroll) for mer informasjon.
				* Tilgang for saksbehandler og system styres gjennom NORG og gruppemedlemskap i AD.""",
				content = @Content(mediaType = APPLICATION_JSON_VALUE)),
		@ApiResponse(responseCode = "404", description = """
				* Dokumentet ble ikke funnet i fagarkivet.
				* Når `variantFormat` ikke oppgis, returneres 404 dersom dokumentet verken har en `SLADDET`- eller `ARKIV`-variant.
				* Dette kan være av midlertidig natur i tilfeller der konsument får en claim check på en journalpostId før den er ferdig arkivert.
				* Det er opp til utvikleren å vurdere om det skal forsøkes retry på denne feilstatusen.""",
				content = @Content(mediaType = APPLICATION_JSON_VALUE))}
)
public @interface SwaggerRestHentDokument {
}
