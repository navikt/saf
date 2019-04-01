package no.nav.saf.anticorruptionlayer.aktoer.aktoerv2;

import static no.nav.saf.anticorruptionlayer.RetryConstants.DELAY_SHORT_AKTOER_V2;
import static no.nav.saf.anticorruptionlayer.RetryConstants.MAX_ATTEMPTS_SHORT_AKTOER_V2;
import static no.nav.saf.anticorruptionlayer.RetryConstants.MULTIPLIER_SHORT_AKTOER_V2;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.aktoer.domain.HentAktoerIdForIdentResponseTo;
import no.nav.saf.anticorruptionlayer.aktoer.domain.HentIdentForAktoerIdListeResponseTo;
import no.nav.saf.anticorruptionlayer.aktoer.domain.HentIdentForAktoerIdResponseTo;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.exceptions.SafTechnicalException;
import no.nav.saf.metrics.Monitor;
import no.nav.tjeneste.virksomhet.aktoer.v2.binding.AktoerV2;
import no.nav.tjeneste.virksomhet.aktoer.v2.binding.HentAktoerIdForIdentPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.aktoer.v2.binding.HentIdentForAktoerIdPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.HentAktoerIdForIdentRequest;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.HentAktoerIdForIdentResponse;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.HentIdentForAktoerIdListeRequest;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.HentIdentForAktoerIdListeResponse;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.HentIdentForAktoerIdRequest;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.HentIdentForAktoerIdResponse;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.IdentDetaljer;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */

@Component
@Slf4j
public class AktoerV2Consumer {

	private final AktoerV2 aktoerV2;

	public AktoerV2Consumer(AktoerV2 aktoerV2) {
		this.aktoerV2 = aktoerV2;
	}

	@Retryable(include = SafTechnicalException.class,
			maxAttempts = MAX_ATTEMPTS_SHORT_AKTOER_V2,
			backoff = @Backoff(delay = DELAY_SHORT_AKTOER_V2, multiplier = MULTIPLIER_SHORT_AKTOER_V2))
	@Monitor(value = "dok_consumer", extraTags = {"process", "hentIdentForAktoerId"}, histogram = true)
	public HentIdentForAktoerIdResponseTo hentIdentForAktoerId(String aktoerId) {
		if(log.isDebugEnabled()) {
			log.debug("henter ident for aktoerId={}", aktoerId);
		}
		HentIdentForAktoerIdRequest request = new HentIdentForAktoerIdRequest();
		request.setAktoerId(aktoerId);
		try {
			HentIdentForAktoerIdResponse response = aktoerV2.hentIdentForAktoerId(request);
			return HentIdentForAktoerIdResponseTo.builder()
					.foedselsnr(response.getIdent())
					.historiskeIdenter(response.getHistoriskeIdenter())
					.build();
		} catch (HentIdentForAktoerIdPersonIkkeFunnet e) {
			throw new SafFunctionalException(String.format("Ident ikke funnet for aktoerId=%s", aktoerId), e);
		} catch (Exception e) {
			throw new SafTechnicalException(String.format("Teknisk feil mot aktoerV2:HentIdentForAktoerId. AktoerId=%s. Feilmelding=%s", aktoerId, e
					.getMessage()), e);
		}
	}

	@Retryable(include = SafTechnicalException.class,
			maxAttempts = MAX_ATTEMPTS_SHORT_AKTOER_V2,
			backoff = @Backoff(delay = DELAY_SHORT_AKTOER_V2, multiplier = MULTIPLIER_SHORT_AKTOER_V2))
	public HentAktoerIdForIdentResponseTo hentAktoerIdForIdent(String ident) {
		if(log.isDebugEnabled()) {
			log.debug("henter aktoerId for ident={}", ident);
		}

		HentAktoerIdForIdentRequest request = new HentAktoerIdForIdentRequest();
		request.setIdent(ident);
		try {
			HentAktoerIdForIdentResponse response = aktoerV2.hentAktoerIdForIdent(request);
			return HentAktoerIdForIdentResponseTo.builder()
					.aktoerId(response.getAktoerId())
					.historiskeIdenter(response.getIdentHistorikk()
							.stream()
							.map(IdentDetaljer::getTpsId)
							.collect(Collectors.toList()))
					.build();
		} catch (HentAktoerIdForIdentPersonIkkeFunnet e) {
			throw new SafFunctionalException(("AktoerId ikke funnet for ident"), e);
		} catch (Exception e) {
			throw new SafTechnicalException(String.format("Teknisk feil mot aktoerV2:HentAktoerIdForIdent.Feilmelding=%s", e
					.getMessage()), e);
		}
	}

	@Retryable(include = SafTechnicalException.class,
			maxAttempts = MAX_ATTEMPTS_SHORT_AKTOER_V2,
			backoff = @Backoff(delay = DELAY_SHORT_AKTOER_V2, multiplier = MULTIPLIER_SHORT_AKTOER_V2))
	@Monitor(value = "dok_consumer", extraTags = {"process", "hentIdentForAktoerId"}, histogram = true)
	public List<HentIdentForAktoerIdListeResponseTo> hentIdentForAktoerIdListe(List<String> aktoerIdListe) {
		if(log.isDebugEnabled()) {
			log.debug("henter ident for aktoerIdListe={}", aktoerIdListe);
		}
		HentIdentForAktoerIdListeRequest request = new HentIdentForAktoerIdListeRequest();
		request.getAktoerIdListe().addAll(aktoerIdListe);
		try {
			HentIdentForAktoerIdListeResponse response = aktoerV2.hentIdentForAktoerIdListe(request);
			checkAndLogErrors(response);

			return response.getIdentListe().stream()
					.map(e -> HentIdentForAktoerIdListeResponseTo.builder()
							.foedselsnr(e.getGjeldendeIdent().getTpsId())
							.aktoerId(e.getAktoerId())
							.historiskeIdenter(e.getHistoriskIdentListe().stream()
									.map(IdentDetaljer::getTpsId)
									.collect(Collectors.toList()))
							.build())
					.collect(Collectors.toList());
		} catch (Exception e) {
			throw new SafTechnicalException(String.format("Teknisk feil mot aktoerV2:HentIdentForAktoerIdListe. AktoerId(er)=%s. Feilmelding=%s", aktoerIdListe, e
					.getMessage()), e);
		}
	}

	private void checkAndLogErrors(HentIdentForAktoerIdListeResponse response) {

		if (response != null && !response.getFeilListe().isEmpty()) {
			StringBuilder feilmelding = new StringBuilder();
			feilmelding.append("Feil ved oppslag mot aktoerV2: Ident ikke funnet for aktørId(er)=");
			response.getFeilListe().forEach(feil -> {
				feilmelding.append(feil.getRequestInput());
				feilmelding.append("(" + feil.getFeilBeskrivelse() + ") ");
			});

			log.warn(feilmelding.toString().trim());
		}
	}
}
