package no.nav.saf.endpoints;

import static no.nav.saf.security.OidcAuthUtils.getOidcToken;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.metrics.DokMetrics;
import no.nav.saf.swagger.SwaggerRestHentDokument;
import no.nav.saf.tilgangskontroll.NavBrukertype;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tjeneste.hentdokument.HentDokumentArguments;
import no.nav.saf.tjeneste.hentdokument.HentDokumentDomainCoordinator;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.Base64;

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
	@DokMetrics(value = "dok_request", description = "rest hentDokument", percentiles = {0.5, 0.95})
	public Base64 hentDokument(@PathVariable String journalpostId,
							   @PathVariable String dokumentId,
							   @PathVariable String variantFormat,
							   @RequestHeader HttpHeaders httpHeaders) {
		String oidcToken = getOidcToken(httpHeaders);

		HentDokumentArguments hentDokumentArguments = HentDokumentArguments.builder()
				.journalpostId(journalpostId)
				.dokumentId(dokumentId)
				.variantFormat(variantFormat)
				.build();

		SafRequestContext safRequestContext = SafRequestContext.builder()
				.oidcToken(oidcToken)
				.navBrukertype(NavBrukertype.SAKSBEHANDLER)
				.build();

		return hentDokumentDomainCoordinator.hentDokument(hentDokumentArguments, safRequestContext).getDokument();
	}
}
