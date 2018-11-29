package no.nav.saf.anticorruptionlayer.pensjonsak;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.pensjonsak.hentsaksammendragliste.PensjonSakConsumer;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tjeneste.visningsmodell.Sak;
import no.nav.saf.tjeneste.visningsmodell.kode.Arkivsakssystem;
import no.nav.saf.tjeneste.visningsmodell.kode.Tema;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
class PensjonSakAntiCorruptionLayerImpl implements PensjonSakAntiCorruptionLayer {

	private final PensjonSakConsumer pensjonSakConsumer;

	@Inject
	public PensjonSakAntiCorruptionLayerImpl(PensjonSakConsumer pensjonSakConsumer) {
		this.pensjonSakConsumer = pensjonSakConsumer;
	}

	@Override
	public List<TilgangSak> hentTilgangSakList(final String foedselsnummer) {
		try {
			return pensjonSakConsumer.hentSakSammendragListe(foedselsnummer).stream()
					.map(tilgangsak -> TilgangSak.builder()
							.arkivsaksnummer(tilgangsak.getSakNr())
							.arkivsaksystem(String.valueOf(Arkivsakssystem.PSAK))
							.tema(tilgangsak.getTema())
							.build())
					.collect(Collectors.toList());
		} catch (Exception e) {
			log.warn("Klarte ikke hente pensjonssaker for foedelsnummer={}", "*****", e);
			return new ArrayList<>();
		}
	}

	@Override
	public List<Sak> hentSakerByFoedselsnummer(final String foedselsnummer) {
		try {
			return pensjonSakConsumer.hentSakSammendragListe(foedselsnummer).stream()
					.map(sakSammendrag -> Sak.builder()
							.arkivsaksnummer(sakSammendrag.getSakNr())
							.arkivsaksystem(Arkivsakssystem.PSAK)
							.fagsaksnummer(sakSammendrag.getSakNr())
							.fagsystem("AT06")
							.tema(Tema.valueOf(sakSammendrag.getTema()))
							.datoOpprettet(sakSammendrag.getDatoOpprettet())
							.build())
					.collect(Collectors.toList());
		} catch (Exception e) {
			log.warn("Klarte ikke hente pensjonssaker for foedelsnummer={}", "*****", e);
			return new ArrayList<>();
		}
	}
}
