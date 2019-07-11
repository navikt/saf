package no.nav.saf.endpoints;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.domain.HentDokument;
import no.nav.saf.domain.kode.Variantformat;
import no.nav.saf.exceptions.HentdokumentTilgangskontrollException;
import no.nav.saf.hentdokument.HentDokumentDomainCoordinator;
import no.nav.saf.metrics.Monitor;
import no.nav.saf.swagger.SwaggerRestHentDokument;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.SafSecurityContext;
import no.nav.saf.tilgangskontroll.validation.OidcValidatorTool;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.UUID;

/**
 * Endepunktet til hentDokument, som returnerer et dokument fra joark basert på journalpostId, dokumentInfoId og variantFormat.
 * Tjenesten er sikret med Abac
 *
 * @author Sigurd Midttun, Visma Consulting.
 */
@RestController
@RequestMapping("rest/")
@Api(tags = "hentdokument API", description = "Tilbyr henting av fysiske dokumenter")
@Slf4j
public class HentDokumentController {
	private static final String X_CORRELATION_ID = "X-Correlation-ID";
	private final HentDokumentDomainCoordinator hentDokumentDomainCoordinator;
	private final OidcValidatorTool oidcValidatorTool;

	@Inject
	public HentDokumentController(HentDokumentDomainCoordinator hentDokumentDomainCoordinator,
								  OidcValidatorTool oidcValidatorTool) {
		this.hentDokumentDomainCoordinator = hentDokumentDomainCoordinator;
		this.oidcValidatorTool = oidcValidatorTool;
	}

	@ApiOperation(value = "Henter fysiske dokumenter fra NAV sitt arkiv og gjør nødvendig tilgangskontroll.", authorizations = {@Authorization(value = "apiKey")})
	@SwaggerRestHentDokument
	@GetMapping(value = "hentdokument/{journalpostId}/{dokumentInfoId}/{variantFormat}")
	@Monitor(value = "dok_request", extraTags = {"process", "hentDokument", "requestType", "hentDokument"}, histogram = true)
	public ResponseEntity<byte[]> hentDokument(@ApiParam(name = "journalpostId", value = "Id for aktuell journalpost", required = true) @PathVariable String journalpostId,
											   @ApiParam(name = "dokumentInfoId", value = "Id for aktuelt dokument", required = true) @PathVariable String dokumentInfoId,
											   @ApiParam(name = "variantFormat", value = "Varianten til dokumentet som skal hentes. [Følg lenken for gyldige verdier](https://confluence.adeo.no/display/BOA/Enum%3A+Variantformat).", required = true) @PathVariable String variantFormat,
											   @ApiParam(name = X_CORRELATION_ID, value = "(Optional) ID til logging og sporing på tvers av verdikjeder.") @RequestHeader(value = X_CORRELATION_ID, required = false) String xCorrelationId,
											   @ApiParam(hidden = true) @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader) {
		SafRequestContext safRequestContext = new SafRequestContext(authorizationHeader, generateCorrelationIdIfNull(xCorrelationId), oidcValidatorTool);
		log.info("hentDokument har mottatt kall. journalpostId={}, dokumentInfoId={}, variantFormat={}", journalpostId, dokumentInfoId, variantFormat);
		try {
			safRequestContext.getSecurityContext().getOidcTokenBody();
			validateServiceUserAccess(safRequestContext, variantFormat);
			HentDokument response = hentDokumentDomainCoordinator.hentDokument(journalpostId, dokumentInfoId, variantFormat, safRequestContext);
			log.info("hentDokument hentet dokument. journalpostId={}, dokumentInfoId={}, variantFormat={}", journalpostId, dokumentInfoId, variantFormat);

			return ResponseEntity.ok()
					.contentType(response.getMediaType())
					.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + dokumentInfoId + "_" + variantFormat + response.getExtension())
					.body(response.getDokument());
		} catch (HentdokumentTilgangskontrollException e) {
			log.warn("hentDokument hentet ikke dokument. journalpostId={}, dokumentInfoId={}, variantFormat={}. Tilgang ble avvist av grunn: " + e.getMessage(), journalpostId, dokumentInfoId, variantFormat);
			throw e;
		}
	}

	private String generateCorrelationIdIfNull(String xCorrelationId) {
		if (xCorrelationId == null || xCorrelationId.trim().isEmpty()) {
			return UUID.randomUUID().toString();
		} else {
			return xCorrelationId;
		}
	}

	private void validateServiceUserAccess(SafRequestContext safRequestContext, String variantFormat) {
		SafSecurityContext securityContext = safRequestContext.getSecurityContext();
		if (securityContext.isPrivilegiedServiceUser()) {
			return;
		}
		if (securityContext.isServiceUser()) {
			if (!Variantformat.ORIGINAL.name().equals(variantFormat)) {
				throw new HentdokumentTilgangskontrollException(
						"Servicebruker forsøker hente dokument med variantFormat=" +
						variantFormat + ". HentDokument tjenesten tillater kun at servicebrukere uten spesiell avtale kun får tilgang til variantFormat=" + Variantformat.ORIGINAL + ".");
			}
		}
	}
}
