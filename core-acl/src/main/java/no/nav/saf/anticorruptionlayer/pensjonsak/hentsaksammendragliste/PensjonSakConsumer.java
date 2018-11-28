package no.nav.saf.anticorruptionlayer.pensjonsak.hentsaksammendragliste;


import static no.nav.saf.anticorruptionlayer.RetryConstants.DELAY_SHORT_PENSJON_V1;
import static no.nav.saf.anticorruptionlayer.RetryConstants.MAX_ATTEMPTS_SHORT_PENSJON_V1;
import static no.nav.saf.anticorruptionlayer.RetryConstants.MULTIPLIER_SHORT_PENSJON_V1;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.pensjonsak.domain.SakSammendragListeTo;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.exceptions.SafTechnicalException;
import no.nav.saf.metrics.Monitor;
import no.nav.saf.tjeneste.visningsmodell.kode.Arkivsakssystem;
import no.nav.tjeneste.virksomhet.pensjonsak.v1.HentSakSammendragListePersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.pensjonsak.v1.HentSakSammendragListeSakManglerEierenhet;
import no.nav.tjeneste.virksomhet.pensjonsak.v1.PensjonSakV1;
import no.nav.tjeneste.virksomhet.pensjonsak.v1.meldinger.WSHentSakSammendragListeRequest;
import no.nav.tjeneste.virksomhet.pensjonsak.v1.meldinger.WSHentSakSammendragListeResponse;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.stream.Collectors;


@Slf4j
@Component
public class PensjonSakConsumer {
	private final PensjonSakV1 pensjonSakV1;
	private static final int MILLI_TO_NANO_CONST = 100000;

	@Inject
	public PensjonSakConsumer(PensjonSakV1 pensjonSakV1) {
		this.pensjonSakV1 = pensjonSakV1;
	}

	@Retryable(include = SafTechnicalException.class,
			maxAttempts = MAX_ATTEMPTS_SHORT_PENSJON_V1,
			backoff = @Backoff(delay = DELAY_SHORT_PENSJON_V1, multiplier = MULTIPLIER_SHORT_PENSJON_V1))
	@Monitor(value = "dok_consumer", extraTags = {"process", "hentSakSammendragListe"}, histogram = true)
	public SakSammendragListeTo hentSakSammendragListe(final String personident) {
		WSHentSakSammendragListeRequest request = new WSHentSakSammendragListeRequest();
		request.setPersonident(personident);

		if (log.isDebugEnabled()) {
			log.debug("Henter psaker for foedselsnummer={}", personident);
		}

		try {
			WSHentSakSammendragListeResponse response = pensjonSakV1.hentSakSammendragListe(request);

			SakSammendragListeTo returnObject = new SakSammendragListeTo();

			returnObject.setSakSammendragListe(response.getSakSammendragListe().stream().map(saksammendrag -> SakSammendragListeTo.SakSammendrag.builder()
					.sakNr(saksammendrag.getSakId())
					.arkivSakSystem(Arkivsakssystem.PSAK)
					.tema(saksammendrag.getArkivtema().getValue())
					.datoOpprettet(saksammendrag.getSaksperiode().getFom() == null ? null : jodaToJavaLocalDateTime(saksammendrag.getSaksperiode().getFom().toDateTimeAtStartOfDay().toLocalDateTime()))
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

	public static java.time.LocalDateTime jodaToJavaLocalDateTime(org.joda.time.LocalDateTime localDateTime) {
		return java.time.LocalDateTime.of(
				localDateTime.getYear(),
				localDateTime.getMonthOfYear(),
				localDateTime.getDayOfMonth(),
				localDateTime.getHourOfDay(),
				localDateTime.getMinuteOfHour(),
				localDateTime.getSecondOfMinute(),
				localDateTime.getMillisOfSecond() * MILLI_TO_NANO_CONST);
	}
}
