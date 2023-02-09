package no.nav.saf.endpoints.rest;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.domain.HentDokument;
import no.nav.saf.domain.kode.Variantformat;
import no.nav.saf.exceptions.DokumentIkkeFunnetException;
import no.nav.saf.exceptions.HentdokumentTilgangskontrollException;
import no.nav.saf.exceptions.JournalpostIkkeFunnetException;
import no.nav.saf.hentdokument.HentDokumentDomainCoordinator;
import no.nav.saf.metrics.AudienceCounter;
import no.nav.saf.metrics.Monitor;
import no.nav.saf.springdoc.SwaggerRestHentDokument;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.SafSecurityContext;
import no.nav.security.token.support.core.api.Protected;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static no.nav.saf.endpoints.HeaderUtils.createNavCallid;
import static no.nav.saf.headers.NavHeaders.NAV_CALLID;
import static no.nav.saf.headers.NavHeaders.NAV_USER_ID;
import static no.nav.saf.headers.NavHeaders.X_CORRELATION_ID;
import static no.nav.saf.util.MDCUtility.addMdcData;

/**
 * Endepunktet til hentDokument, som returnerer et dokument fra joark basert på journalpostId, dokumentInfoId og variantFormat.
 * Tjenesten er sikret med Oauth2 flyt tokens.
 */
@Tag(name="saf REST API", description = "Lesemodellen til fagarkivet. Henter dokumenter.")
@Protected
@RestController
@RequestMapping("rest/")
@Slf4j
public class HentDokumentController {
	private final HentDokumentDomainCoordinator hentDokumentDomainCoordinator;
	private final TokenValidationContextHolder tokenValidationContextHolder;
	private final AudienceCounter audienceCounter;
	private final Map<String, Boolean> privilegiedServiceusers;

	@Autowired
	public HentDokumentController(@Qualifier("privilegiedServiceusers") Map<String, Boolean> privilegiedServiceusers,
								  HentDokumentDomainCoordinator hentDokumentDomainCoordinator,
								  AudienceCounter audienceCounter,
								  TokenValidationContextHolder tokenValidationContextHolder) {
		this.tokenValidationContextHolder = tokenValidationContextHolder;
		this.hentDokumentDomainCoordinator = hentDokumentDomainCoordinator;
		this.audienceCounter = audienceCounter;
		this.privilegiedServiceusers = privilegiedServiceusers;
	}

	@SwaggerRestHentDokument
	@GetMapping(value = "hentdokument/{journalpostId}/{dokumentInfoId}/{variantFormat}")
	@Monitor(value = "dok_request", extraTags = {"process", "hentDokument", "requestType", "hentDokument"}, histogram = true)
	public ResponseEntity<byte[]> hentDokument(
			@Parameter(name = "journalpostId", description = "Id for aktuell journalpost", required = true) @PathVariable String journalpostId,
			@Parameter(name = "dokumentInfoId", description = "Id for aktuelt dokument", required = true) @PathVariable String dokumentInfoId,
			@Parameter(name = "variantFormat", description = "Varianten til dokumentet som skal hentes. [Følg lenken for gyldige verdier](https://confluence.adeo.no/display/BOA/Enum%3A+Variantformat).", required = true) @PathVariable String variantFormat,
			@Parameter(name = NAV_CALLID, description = "(Valgfri) ID for logging og sporing på tvers av verdikjeder. Eksempel: UUID") @RequestHeader(value = NAV_CALLID, required = false) String navCallid,
			@Parameter(name = NAV_USER_ID, description = "(Valgfri) NAV ident som overstyrer sporing for kall fra servicebrukere.") @RequestHeader(value = NAV_USER_ID, required = false) String navUserId,
			@Parameter(name = X_CORRELATION_ID, description = "@Deprecated. Bruk " + NAV_CALLID, hidden = true) @RequestHeader(value = X_CORRELATION_ID, required = false) String xCorrelationId
	) {
		final SafRequestContext safRequestContext = new SafRequestContext(createNavCallid(navCallid, xCorrelationId),
				navUserId,
				tokenValidationContextHolder.getTokenValidationContext(),
				privilegiedServiceusers
		);
		addMdcData(safRequestContext);
		log.info("hentDokument har mottatt kall. journalpostId={}, dokumentInfoId={}, variantFormat={}", journalpostId, dokumentInfoId, variantFormat);
		try {
			audienceCounter.increment(
					safRequestContext.getSecurityContext().getIssuer(),
					safRequestContext.getSecurityContext().getAudience()
			);

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
		} catch (JournalpostIkkeFunnetException | DokumentIkkeFunnetException e) {
			log.warn("hentDokument fant ikke dokument tilknyttet journalpost. journalpostId={}, dokumentInfoId={}, variantFormat={}. " + e.getMessage(), journalpostId, dokumentInfoId, variantFormat);
			throw e;
		} catch (Exception e) {
			log.error("hentDokument hentet ikke dokument. journalpostId={}, dokumentInfoId={}, variantFormat={}. Ukjent teknisk feil: " + e.getMessage(), journalpostId, dokumentInfoId, variantFormat, e);
			throw e;
		} finally {
			MDC.clear();
		}
	}

	private void validateServiceUserAccess(SafRequestContext safRequestContext, String variantFormat) {
		SafSecurityContext securityContext = safRequestContext.getSecurityContext();
		if (securityContext.isPrivilegiedServiceUserWithArkivVariantAccess() || securityContext.isJwtAzureClientCredentialFlow()) {
			// Azure client credential flow roller blir sjekket etter at journalpost er hentet.
			return;
		}
		if (securityContext.isSystem() && !Variantformat.ORIGINAL.name().equals(variantFormat)) {
			throw new HentdokumentTilgangskontrollException(
					"Servicebruker forsøker å hente dokument med variantFormat=" +
							variantFormat + ". Servicebrukere har kun tilgang til variantFormat=" + Variantformat.ORIGINAL +
							" med mindre man har en avtale med Team Dokumentløsninger. Snakk med oss om behov.");
		}
	}
}
