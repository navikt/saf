package no.nav.saf.anticorruptionlayer.pensjonsak.hentsaksammendragliste;


import no.nav.saf.cache.LokalCacheConfig;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.exceptions.SafTechnicalException;
import no.nav.tjeneste.virksomhet.pensjonsak.v1.HentSakSammendragListePersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.pensjonsak.v1.HentSakSammendragListeSakManglerEierenhet;
import no.nav.tjeneste.virksomhet.pensjonsak.v1.PensjonSakV1;
import no.nav.tjeneste.virksomhet.pensjonsak.v1.informasjon.WSSakSammendrag;
import no.nav.tjeneste.virksomhet.pensjonsak.v1.meldinger.WSHentSakSammendragListeRequest;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;


@Component
public class SakSammendragConsumer {
	private final PensjonSakV1 pensjonSakV1;

	@Inject
	public SakSammendragConsumer(PensjonSakV1 pensjonSakV1) {
		this.pensjonSakV1 = pensjonSakV1;

	}

	@Cacheable(cacheNames = LokalCacheConfig.SAKER_BY_AKTOER_ID_CACHE, key = "#personident")
	public List<WSSakSammendrag> hentSakSammendragListe(String personident) {
		WSHentSakSammendragListeRequest request = new WSHentSakSammendragListeRequest();
		request.setPersonident(personident);

		try {
			return pensjonSakV1.hentSakSammendragListe(request).getSakSammendragListe();
		} catch (HentSakSammendragListeSakManglerEierenhet e) {
			throw new SafTechnicalException(String.format("Funksjonell feil mot Pensjon_v1. En person med ident =%s ble funnet, men en av sakene mangler eierenhet.", personident), e);
		} catch (HentSakSammendragListePersonIkkeFunnet e) {
			throw new SafFunctionalException(String.format("Teknisk feil mot Pensjon_v1. En person med ident=%s ble ikke funnet. Feilmelding=%s", personident, e
					.getMessage()), e);
		}
	}
}
