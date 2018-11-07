package no.nav.saf.anticorruptionlayer.pensjonsak.hentsaksammendragliste;


import static no.nav.saf.anticorruptionlayer.RetryConstants.DELAY_SHORT_PENSJON_V1;
import static no.nav.saf.anticorruptionlayer.RetryConstants.MAX_ATTEMPTS_SHORT_PENSJON_V1;
import static no.nav.saf.anticorruptionlayer.RetryConstants.MULTIPLIER_SHORT_PENSJON_V1;

import no.nav.saf.anticorruptionlayer.pensjonsak.domain.SakSammendragListeTo;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.exceptions.SafTechnicalException;
import no.nav.saf.tjeneste.visningsmodell.kode.Arkivsakssystem;
import no.nav.tjeneste.virksomhet.pensjonsak.v1.binding.HentSakSammendragListePersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.pensjonsak.v1.binding.HentSakSammendragListeSakManglerEierenhet;
import no.nav.tjeneste.virksomhet.pensjonsak.v1.binding.PensjonSakV1;
import no.nav.tjeneste.virksomhet.pensjonsak.v1.meldinger.HentSakSammendragListeRequest;
import no.nav.tjeneste.virksomhet.pensjonsak.v1.meldinger.HentSakSammendragListeResponse;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.stream.Collectors;


@Component
public class PensjonSakConsumer {
	private final PensjonSakV1 pensjonSakV1;

	@Inject
	public PensjonSakConsumer(PensjonSakV1 pensjonSakV1) {
		this.pensjonSakV1 = pensjonSakV1;

	}

	@Retryable(include = SafTechnicalException.class,
			maxAttempts = MAX_ATTEMPTS_SHORT_PENSJON_V1,
			backoff = @Backoff(delay = DELAY_SHORT_PENSJON_V1, multiplier = MULTIPLIER_SHORT_PENSJON_V1))
	public SakSammendragListeTo hentSakSammendragListe(String personident) {
		HentSakSammendragListeRequest request = new HentSakSammendragListeRequest();
		request.setPersonident(personident);

		try {
			HentSakSammendragListeResponse response = pensjonSakV1.hentSakSammendragListe(request);

			SakSammendragListeTo returnObject = new SakSammendragListeTo();

			returnObject.setSakSammendragListe(response.getSakSammendragListe().stream().map(saksammendrag -> SakSammendragListeTo.SakSammendrag.builder()
					.sakNr(saksammendrag.getSakId())
					.arkivSakSystem(Arkivsakssystem.PSAK)
					.tema(saksammendrag.getArkivtema().toString())
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
