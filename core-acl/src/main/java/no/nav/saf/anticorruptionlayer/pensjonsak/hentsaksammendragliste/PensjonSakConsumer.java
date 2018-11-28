package no.nav.saf.anticorruptionlayer.pensjonsak.hentsaksammendragliste;


import static no.nav.saf.anticorruptionlayer.RetryConstants.DELAY_SHORT_PENSJON_V1;
import static no.nav.saf.anticorruptionlayer.RetryConstants.MAX_ATTEMPTS_SHORT_PENSJON_V1;
import static no.nav.saf.anticorruptionlayer.RetryConstants.MULTIPLIER_SHORT_PENSJON_V1;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.pensjonsak.domain.SakSammendragListeTo;
import no.nav.saf.cache.LokalCacheConfig;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.exceptions.SafTechnicalException;
import no.nav.saf.metrics.Monitor;
import no.nav.saf.tjeneste.visningsmodell.kode.Arkivsakssystem;
import no.nav.tjeneste.virksomhet.pensjonsak.v1.binding.HentSakSammendragListePersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.pensjonsak.v1.binding.HentSakSammendragListeSakManglerEierenhet;
import no.nav.tjeneste.virksomhet.pensjonsak.v1.binding.PensjonSakV1;
import no.nav.tjeneste.virksomhet.pensjonsak.v1.meldinger.HentSakSammendragListeRequest;
import no.nav.tjeneste.virksomhet.pensjonsak.v1.meldinger.HentSakSammendragListeResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PensjonSakConsumer {
	private final PensjonSakV1 pensjonSakV1;

	@Inject
	public PensjonSakConsumer(PensjonSakV1 pensjonSakV1) {
		this.pensjonSakV1 = pensjonSakV1;
	}

	@Cacheable(cacheNames = LokalCacheConfig.PENSJON_SAK_SAMMENDRAG_LISTE_CACHE, key = "#personident")
	@Retryable(include = SafTechnicalException.class,
			maxAttempts = MAX_ATTEMPTS_SHORT_PENSJON_V1,
			backoff = @Backoff(delay = DELAY_SHORT_PENSJON_V1, multiplier = MULTIPLIER_SHORT_PENSJON_V1))
	@Monitor(value = "dok_consumer", extraTags = {"process", "hentSakSammendragListe"}, histogram = true)
	public SakSammendragListeTo hentSakSammendragListe(final String personident) {
		HentSakSammendragListeRequest request = new HentSakSammendragListeRequest();
		request.setPersonident(personident);

		if(log.isDebugEnabled()) {
			log.debug("Henter psaker for foedselsnummer={}", personident);
		}

		try {
			HentSakSammendragListeResponse response = pensjonSakV1.hentSakSammendragListe(request);

			SakSammendragListeTo returnObject = new SakSammendragListeTo();

			returnObject.setSakSammendragListe(response.getSakSammendragListe().stream().map(saksammendrag -> SakSammendragListeTo.SakSammendrag.builder()
					.sakNr(saksammendrag.getSakId())
					.arkivSakSystem(Arkivsakssystem.PSAK)
					.tema(saksammendrag.getArkivtema().getValue())
					.datoOpprettet(saksammendrag.getSaksperiode().getFom() == null ? null : saksammendrag.getSaksperiode().getFom().toGregorianCalendar().toZonedDateTime().toLocalDateTime())
					.build())
					.collect(Collectors.toList())
			);
			return returnObject;
		} catch (HentSakSammendragListeSakManglerEierenhet e) {
			throw new SafTechnicalException("Funksjonell feil mot Pensjon_v1. Personen ble funnet, men en av sakene mangler eierenhet. Feilmelding=%s", e);
		} catch (HentSakSammendragListePersonIkkeFunnet e) {
			throw new SafFunctionalException(String.format("Teknisk feil mot Pensjon_v1. Personen ble ikke funnet. Feilmelding=%s", e
					.getMessage()), e);
		}
	}
}
