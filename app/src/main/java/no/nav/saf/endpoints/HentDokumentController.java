package no.nav.saf.endpoints;

import static no.nav.saf.security.OidcAuthUtils.getOidcToken;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.tilgangskontroll.NavBrukertype;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tjeneste.hentdokument.HentDokumentArguments;
import no.nav.saf.tjeneste.hentdokument.HentDokumentDomainCoordinator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;

/**
 * Endepunktet til hentDokument, som returnerer et dokument fra joark basert på journalpostId, dokumentInfoId og variantFormat".
 * Tjenesten er sikret med Abac
 *
 * @author Sigurd Midttun, Visma Consulting.
 */
@RestController
@Slf4j
public class HentDokumentController {

	private final HentDokumentDomainCoordinator hentDokumentDomainCoordinator;

	public HentDokumentController(HentDokumentDomainCoordinator hentDokumentDomainCoordinator) {
		this.hentDokumentDomainCoordinator = hentDokumentDomainCoordinator;
	}

	//TODO Media type må settes på header!
	@GetMapping(value = "/hentdokument/{journalpostId}/{dokumentInfoId}/{variantFormat}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public Base64 graphQLRequest(HentDokumentDomainCoordinator hentDokumentDomainCoordinator,
								 @PathVariable String journalpostId,
								 @PathVariable String dokumentId,
								 @PathVariable String variantFormat,
								 @RequestHeader HttpHeaders httpHeaders) {
		String oidcToken = getOidcToken(httpHeaders);

		//TODO: Validering av input - regex i path?
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
