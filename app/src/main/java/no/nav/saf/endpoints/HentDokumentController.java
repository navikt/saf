package no.nav.saf.endpoints;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.domain.HentDokument;
import no.nav.saf.hentdokument.HentDokumentDomainCoordinator;
import no.nav.saf.metrics.Monitor;
import no.nav.saf.swagger.SwaggerRestHentDokument;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.validation.OidcValidatorTool;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

/**
 * Endepunktet til hentDokument, som returnerer et dokument fra joark basert på journalpostId, dokumentInfoId og variantFormat".
 * Tjenesten er sikret med Abac
 *
 * @author Sigurd Midttun, Visma Consulting.
 */
@RestController
@RequestMapping("rest/")
@Api(tags = "hentdokument API", description = "Tilbyr henting av fysiske dokumenter")
@Slf4j
public class HentDokumentController {
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
											   @ApiParam(name = "variantFormat", value = "Format på dokumentet som skal hentes eg. ARKIV, SLADDET m.fl.", required = true) @PathVariable String variantFormat,
											   @ApiParam(hidden = true) @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader) {

		log.info("hentDokument har mottatt kall. journalpostId={}, dokumentInfoId={}, variantFormat={}", journalpostId, dokumentInfoId, variantFormat);
		HentDokument response = hentDokumentDomainCoordinator.hentDokument(journalpostId, dokumentInfoId, variantFormat, new SafRequestContext(authorizationHeader, oidcValidatorTool));

		return ResponseEntity.ok()
				.contentType(response.getMediaType())
				.header("content-disposition", "inline; filename=" + dokumentInfoId + "_" + variantFormat)
				.body(response.getDokument());
	}

}
