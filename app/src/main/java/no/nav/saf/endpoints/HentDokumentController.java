package no.nav.saf.endpoints;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.metrics.Monitor;
import no.nav.saf.swagger.SwaggerRestHentDokument;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tjeneste.hentdokument.HentDokument;
import no.nav.saf.tjeneste.hentdokument.HentDokumentDomainCoordinator;
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
@Slf4j
public class HentDokumentController {
	private final HentDokumentDomainCoordinator hentDokumentDomainCoordinator;

	@Inject
	public HentDokumentController(HentDokumentDomainCoordinator hentDokumentDomainCoordinator) {
		this.hentDokumentDomainCoordinator = hentDokumentDomainCoordinator;
	}

	@ApiOperation(value = "Hent dokument for angitte søkekriterier", authorizations = {@Authorization(value = "apiKey")})
	@SwaggerRestHentDokument
	@GetMapping(value = "hentdokument/{journalpostId}/{dokumentId}/{variantFormat}")
	@Monitor(value = "dok_request", extraTags = {"process", "hentDokument"}, percentiles = {0.9, 0.95})
	public ResponseEntity<byte[]> hentDokument(@PathVariable String journalpostId,
											   @PathVariable String dokumentId,
											   @PathVariable String variantFormat,
											   @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader) {

		log.info("hentDokument har mottatt forespørsel om å hente dokument, journalpostId={}, dokumentId={}, variantFormat={}", journalpostId, dokumentId, variantFormat);
		HentDokument response = hentDokumentDomainCoordinator.hentDokument(journalpostId, dokumentId, variantFormat, new SafRequestContext(authorizationHeader));

		return ResponseEntity.ok()
				.contentType(response.getMediaType())
				.header("content-disposition", "inline; filename=" + dokumentId + "_" + variantFormat)
				.body(response.getDokument());
	}

}
